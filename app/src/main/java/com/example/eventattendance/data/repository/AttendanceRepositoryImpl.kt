package com.example.eventattendance.data.repository

import android.content.Context
import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource
import com.example.eventattendance.data.remote.dto.EventAttendanceItemDto
import com.example.eventattendance.data.remote.dto.MarkAttendanceResponseDto
import com.example.eventattendance.data.remote.dto.SyncAllEventsAttendanceResponseDto
import com.example.eventattendance.di.IoDispatcher
import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.Event
import com.example.eventattendance.domain.model.EventDetailData
import com.example.eventattendance.domain.model.EventStatus
import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.domain.repository.AttendanceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.eventattendance.data.remote.dto.AttendanceDto
import com.example.eventattendance.data.remote.dto.EventDto
import com.example.eventattendance.data.remote.dto.ParticipantDto
import com.example.eventattendance.util.DateUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeOldRecord
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val rpcRemoteDataSource: RpcRemoteDataSource,
    private val supabaseClient: SupabaseClient,
    private val participantCacheRepositoryProvider: Provider<ParticipantCacheRepository>,
    private val attendanceMatrixRepositoryProvider: Provider<AttendanceMatrixRepository>,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AttendanceRepository {

    private val realtimeScope = CoroutineScope(ioDispatcher + SupervisorJob())

    private val eventsCacheFile = File(context.filesDir, "events_cache.json")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = false
    }

    private val _eventsWithStats = MutableStateFlow<List<EventWithStats>>(emptyList())
    override val eventsWithStatsFlow: StateFlow<List<EventWithStats>> = _eventsWithStats.asStateFlow()

    private val _eventDetailsMap = MutableStateFlow<Map<Int, EventDetailData>>(emptyMap())

    init {
        // Load initial events from local cache on startup if available
        loadCachedEvents()
        startRealtimeSubscription()
    }

    private fun startRealtimeSubscription() {
        realtimeScope.launch {
            while (isActive) {
                try {
                    val channel = supabaseClient.channel("attendance-realtime-sync")
                    val attendanceChangeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "attendance"
                    }
                    val eventsChangeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                        table = "events"
                    }
                    channel.subscribe()

                    launch {
                        attendanceChangeFlow.collect { action ->
                            when (action) {
                                is PostgresAction.Insert -> {
                                    runCatching {
                                        val record = action.decodeRecord<AttendanceDto>()
                                        handleRemoteAttendanceInsert(record)
                                    }
                                }
                                is PostgresAction.Delete -> {
                                    runCatching {
                                        val record = action.decodeOldRecord<AttendanceDto>()
                                        handleRemoteAttendanceDelete(record)
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }

                    launch {
                        eventsChangeFlow.collect { action ->
                            when (action) {
                                is PostgresAction.Insert, is PostgresAction.Update -> {
                                    runCatching {
                                        val record = action.decodeRecord<EventDto>()
                                        updateLocalEvent(
                                            record.eventId,
                                            record.eventName,
                                            record.startsFrom ?: "",
                                            record.endsOn ?: ""
                                        )
                                        runCatching {
                                            attendanceMatrixRepositoryProvider.get().updateLocalEvent(
                                                record.eventId,
                                                record.eventName,
                                                record.startsFrom ?: "",
                                                record.endsOn ?: ""
                                            )
                                        }
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }

                    awaitCancellation()
                } catch (e: Exception) {
                    if (!isActive) break
                    delay(5000L)
                }
            }
        }
    }

    private fun handleRemoteAttendanceInsert(att: AttendanceDto) {
        val eventId = att.eventId
        val pId = att.pId
        val time = att.time ?: DateUtils.getCurrentIsoTime()

        // Always update attendance matrix repository so Admin & SPOC views stay in sync
        runCatching {
            attendanceMatrixRepositoryProvider.get().addLocalScan(eventId, pId)
        }

        val currentDetail = _eventDetailsMap.value[eventId]
        val alreadyPresent = currentDetail?.attendees?.any { it.participantId == pId } == true

        // If local device already recorded this scan optimistically, do not double-increment
        if (alreadyPresent) {
            return
        }

        val participant = runCatching {
            participantCacheRepositoryProvider.get().getParticipant(pId)
        }.getOrNull()

        val name = participant?.name ?: pId
        val teamId = participant?.teamId
        val teamName = participant?.teamName

        val newRecord = AttendanceRecord(
            id = "${eventId}_${pId}",
            eventId = eventId.toString(),
            participantId = pId,
            checkedInAt = time,
            status = "present",
            participantName = name,
            participantUsn = pId,
            teamId = teamId,
            teamName = teamName,
            isPendingSync = false
        )

        val existingAttendees = currentDetail?.attendees ?: emptyList()
        val updatedAttendees = listOf(newRecord) + existingAttendees.filterNot { it.participantId == pId }
        val updatedScanned = (currentDetail?.scannedCount ?: 0) + 1
        val totalExpected = if ((currentDetail?.totalExpected ?: 0) > 0) {
            currentDetail!!.totalExpected
        } else {
            runCatching { participantCacheRepositoryProvider.get().getAllParticipants().size }.getOrDefault(0)
        }
        val updatedProgress = if (totalExpected > 0) "$updatedScanned/$totalExpected" else "$updatedScanned"

        val updatedDetail = (currentDetail ?: EventDetailData(
            eventId = eventId,
            eventName = "Event $eventId"
        )).copy(
            scannedCount = updatedScanned,
            totalExpected = totalExpected,
            progress = updatedProgress,
            attendees = updatedAttendees
        )
        _eventDetailsMap.value = _eventDetailsMap.value + (eventId to updatedDetail)

        _eventsWithStats.value = _eventsWithStats.value.map { item ->
            if (item.event.eventId == eventId) {
                val newCount = item.scannedCount + 1
                item.copy(
                    scannedCount = newCount,
                    totalEligibleCount = if (totalExpected > 0) totalExpected else item.totalEligibleCount
                )
            } else {
                item
            }
        }
    }

    private fun handleRemoteAttendanceDelete(att: AttendanceDto) {
        val eventId = att.eventId
        val pId = att.pId

        val currentDetail = _eventDetailsMap.value[eventId] ?: return
        val existingAttendees = currentDetail.attendees
        if (existingAttendees.none { it.participantId == pId }) return

        val updatedAttendees = existingAttendees.filterNot { it.participantId == pId }
        val updatedScanned = (currentDetail.scannedCount - 1).coerceAtLeast(0)
        val totalExpected = currentDetail.totalExpected
        val updatedProgress = if (totalExpected > 0) "$updatedScanned/$totalExpected" else "$updatedScanned"

        val updatedDetail = currentDetail.copy(
            scannedCount = updatedScanned,
            progress = updatedProgress,
            attendees = updatedAttendees
        )
        _eventDetailsMap.value = _eventDetailsMap.value + (eventId to updatedDetail)

        _eventsWithStats.value = _eventsWithStats.value.map { item ->
            if (item.event.eventId == eventId) {
                item.copy(scannedCount = (item.scannedCount - 1).coerceAtLeast(0))
            } else {
                item
            }
        }
    }

    private fun loadCachedEvents() {
        if (eventsCacheFile.exists()) {
            try {
                val content = eventsCacheFile.readText()
                if (content.isNotBlank()) {
                    val response = json.decodeFromString<SyncAllEventsAttendanceResponseDto>(content)
                    mapAndEmitEvents(response.events)
                }
            } catch (_: Exception) {}
        }
    }

    private fun mapAndEmitEvents(events: List<EventAttendanceItemDto>) {
        val mappedEvents = events.map { item ->
            val event = Event(
                id = item.eventId.toString(),
                eventId = item.eventId,
                name = item.eventName ?: "Event ${item.eventId}",
                startsFrom = item.startsFrom,
                endsOn = item.endsOn,
                status = EventStatus.OPEN
            )
            val scanned = item.eventAttendance ?: item.presentPIds.size
            val total = item.totalParticipants ?: 0
            EventWithStats(
                event = event,
                scannedCount = scanned,
                totalEligibleCount = total,
                pendingSyncCount = 0
            )
        }
        _eventsWithStats.value = ProgressCalculator.sortEventsByTime(
            mappedEvents,
            { it.event.startsFrom },
            { it.event.endsOn },
            { it.event.eventId }
        )

        // Also pre-populate event details (replace rather than accumulate old events)
        val detailsMap = mutableMapOf<Int, EventDetailData>()
        for (item in events) {
            val attendees = if (item.attendanceDetails.isNotEmpty()) {
                item.attendanceDetails.map { detail ->
                    AttendanceRecord(
                        id = "${item.eventId}_${detail.pId}",
                        eventId = item.eventId.toString(),
                        participantId = detail.pId,
                        checkedInAt = detail.time ?: "",
                        status = "present",
                        participantName = detail.name ?: detail.pId,
                        participantUsn = detail.pId,
                        teamId = detail.teamId,
                        teamName = detail.teamName,
                        isPendingSync = false
                    )
                }.reversed()
            } else {
                item.presentPIds.map { pId ->
                    AttendanceRecord(
                        id = "${item.eventId}_$pId",
                        eventId = item.eventId.toString(),
                        participantId = pId,
                        checkedInAt = "",
                        status = "present",
                        participantName = pId,
                        participantUsn = pId,
                        isPendingSync = false
                    )
                }.reversed()
            }

            val scanned = item.eventAttendance ?: item.presentPIds.size
            val total = item.totalParticipants ?: 0
            detailsMap[item.eventId] = EventDetailData(
                eventId = item.eventId,
                eventName = item.eventName ?: "Event ${item.eventId}",
                startsFrom = item.startsFrom,
                endsOn = item.endsOn,
                scannedCount = scanned,
                totalExpected = total,
                progress = item.progress ?: "$scanned/$total",
                attendees = attendees
            )
        }
        _eventDetailsMap.value = detailsMap
    }

    override suspend fun syncAllEventsAttendance(forcePull: Boolean): Result<List<EventWithStats>> = withContext(ioDispatcher) {
        runCatching {
            val response = rpcRemoteDataSource.syncAllEventsAttendance()
            // Always persist and emit events, even when empty (handles truncated or reset DB)
            val jsonString = json.encodeToString(response)
            eventsCacheFile.writeText(jsonString)
            mapAndEmitEvents(response.events)
            _eventsWithStats.value
        }.onFailure {
            // Offline fallback: load cached data if present
            if (_eventsWithStats.value.isEmpty()) {
                loadCachedEvents()
            }
        }
    }

    override suspend fun syncEventAttendance(eventId: Int): Result<EventDetailData> = withContext(ioDispatcher) {
        // 1. Try sync_event_attendance RPC
        val rpcResult = runCatching {
            val response = rpcRemoteDataSource.syncEventAttendance(eventId)
            val attendees = response.attendanceDetails.map { detail ->
                AttendanceRecord(
                    id = "${eventId}_${detail.pId}",
                    eventId = eventId.toString(),
                    participantId = detail.pId,
                    checkedInAt = detail.time ?: "",
                    status = "present",
                    participantName = detail.name ?: detail.pId,
                    participantUsn = detail.pId,
                    teamId = detail.teamId,
                    teamName = detail.teamName,
                    isPendingSync = false
                )
            }.reversed()

            val scanned = response.eventAttendance ?: response.presentPIds.size
            val total = response.totalParticipants ?: 0
            val eventData = EventDetailData(
                eventId = eventId,
                eventName = response.eventName ?: "Event $eventId",
                scannedCount = scanned,
                totalExpected = total,
                progress = response.progress ?: "$scanned/$total",
                attendees = attendees
            )

            _eventDetailsMap.value = _eventDetailsMap.value + (eventId to eventData)

            // Reconcile stats in events list
            _eventsWithStats.value = _eventsWithStats.value.map { item ->
                if (item.event.eventId == eventId) {
                    item.copy(
                        scannedCount = scanned,
                        totalEligibleCount = if (total > 0) total else item.totalEligibleCount
                    )
                } else {
                    item
                }
            }

            eventData
        }

        if (rpcResult.isSuccess) {
            return@withContext rpcResult
        }

        // 2. Fallback: syncAllEventsAttendance() which provides all events with attendance
        val allEventsResult = runCatching {
            syncAllEventsAttendance(forcePull = true)
            val detail = _eventDetailsMap.value[eventId]
            if (detail != null) {
                return@runCatching detail
            }
            throw IllegalStateException("Event $eventId not found in sync_all_events_attendance")
        }

        if (allEventsResult.isSuccess) {
            return@withContext allEventsResult
        }

        // 3. Fallback: Query Supabase public.events and public.attendance directly
        val directResult = runCatching {
            val eventDto = supabaseClient.from("events").select {
                filter { eq("event_id", eventId) }
            }.decodeSingleOrNull<EventDto>()

            val attendanceList = runCatching {
                supabaseClient.from("attendance").select {
                    filter { eq("event_id", eventId) }
                }.decodeList<AttendanceDto>()
            }.getOrDefault(emptyList())

            val attendees = attendanceList.map { att ->
                val pId = att.pId
                AttendanceRecord(
                    id = "${eventId}_${pId}",
                    eventId = eventId.toString(),
                    participantId = pId,
                    checkedInAt = att.time ?: "",
                    status = "present",
                    participantName = pId,
                    participantUsn = pId,
                    isPendingSync = false
                )
            }.reversed()

            val scanned = attendees.size
            val eventData = EventDetailData(
                eventId = eventId,
                eventName = eventDto?.eventName ?: "Event $eventId",
                startsFrom = eventDto?.startsFrom,
                endsOn = eventDto?.endsOn,
                scannedCount = scanned,
                totalExpected = scanned,
                progress = "$scanned",
                attendees = attendees
            )

            _eventDetailsMap.value = _eventDetailsMap.value + (eventId to eventData)
            eventData
        }

        if (directResult.isSuccess) {
            return@withContext directResult
        }

        // 4. Fallback: Return cached detail if available
        _eventDetailsMap.value[eventId]?.let { return@withContext Result.success(it) }

        rpcResult
    }

    override fun getEventAttendanceFlow(eventId: Int): Flow<EventDetailData?> {
        return _eventDetailsMap.map { it[eventId] }
    }

    override fun updateLocalEventTimeWindow(eventId: Int, name: String?, startsFrom: String, endsOn: String) {
        updateLocalEvent(eventId, name, startsFrom, endsOn)
    }

    override fun updateLocalEvent(eventId: Int, name: String?, startsFrom: String, endsOn: String) {
        val trimmedName = if (!name.isNullOrBlank()) name.trim() else null

        // 1. Update _eventDetailsMap
        val existingDetail = _eventDetailsMap.value[eventId]
        val updatedDetail = if (existingDetail != null) {
            existingDetail.copy(
                eventName = trimmedName ?: existingDetail.eventName,
                startsFrom = startsFrom,
                endsOn = endsOn
            )
        } else {
            EventDetailData(
                eventId = eventId,
                eventName = trimmedName ?: "Event $eventId",
                startsFrom = startsFrom,
                endsOn = endsOn
            )
        }
        _eventDetailsMap.value = _eventDetailsMap.value + (eventId to updatedDetail)

        // 2. Update _eventsWithStats
        val found = _eventsWithStats.value.any { it.event.eventId == eventId }
        val updatedList = if (found) {
            _eventsWithStats.value.map { item ->
                if (item.event.eventId == eventId) {
                    item.copy(
                        event = item.event.copy(
                            name = trimmedName ?: item.event.name,
                            startsFrom = startsFrom,
                            endsOn = endsOn
                        )
                    )
                } else {
                    item
                }
            }
        } else {
            val newEvent = Event(
                id = eventId.toString(),
                eventId = eventId,
                name = trimmedName ?: "Event $eventId",
                startsFrom = startsFrom,
                endsOn = endsOn,
                status = EventStatus.OPEN
            )
            _eventsWithStats.value + EventWithStats(
                event = newEvent,
                scannedCount = 0,
                totalEligibleCount = 0,
                pendingSyncCount = 0
            )
        }
        _eventsWithStats.value = ProgressCalculator.sortEventsByTime(
            updatedList,
            { it.event.startsFrom },
            { it.event.endsOn },
            { it.event.eventId }
        )

        // 3. Persist to cache file so restarts retain the edit
        runCatching {
            val dtoList = _eventsWithStats.value.map { item ->
                EventAttendanceItemDto(
                    eventId = item.event.eventId,
                    eventName = item.event.name,
                    startsFrom = item.event.startsFrom,
                    endsOn = item.event.endsOn,
                    eventAttendance = item.scannedCount,
                    totalParticipants = item.totalEligibleCount
                )
            }
            val cacheDto = SyncAllEventsAttendanceResponseDto(
                status = "success",
                totalEvents = dtoList.size,
                events = dtoList
            )
            eventsCacheFile.writeText(json.encodeToString(cacheDto))
        }
    }

    override suspend fun markAttendance(
        eventId: Int,
        pIds: List<String>,
        role: String
    ): Result<MarkAttendanceResponseDto> = withContext(ioDispatcher) {
        // 1. First attempt: Call Supabase RPC mark_attendance
        val rpcAttempt = runCatching {
            rpcRemoteDataSource.markAttendance(eventId, pIds, role)
        }
        if (rpcAttempt.isSuccess) {
            return@withContext rpcAttempt
        }

        // 2. Fallback attempt: If RPC fails or is missing, query/insert public.attendance directly
        runCatching {
            val nowIso = DateUtils.getCurrentIsoTime()

            // Check existing attendance records for these participants in this event
            val existing = runCatching {
                supabaseClient.from("attendance").select {
                    filter {
                        eq("event_id", eventId)
                        isIn("p_id", pIds)
                    }
                }.decodeList<AttendanceDto>()
            }.getOrDefault(emptyList())

            val existingPids = existing.map { it.pId }.toSet()
            val toInsert = pIds.filterNot { it in existingPids }

            if (toInsert.isNotEmpty()) {
                val insertDtos = toInsert.map { pId ->
                    AttendanceDto(pId = pId, eventId = eventId, time = nowIso)
                }
                supabaseClient.from("attendance").insert(insertDtos)
            }

            // Get total attendance count for this event
            val totalEventAttendance = runCatching {
                supabaseClient.from("attendance").select {
                    filter { eq("event_id", eventId) }
                }.decodeList<AttendanceDto>().map { it.pId }.distinct().size
            }.getOrDefault(existingPids.size + toInsert.size)

            val totalParticipantsCount = runCatching {
                supabaseClient.from("participants").select().decodeList<ParticipantDto>().size
            }.getOrDefault(0)

            val status = when {
                toInsert.isNotEmpty() -> "success"
                existingPids.isNotEmpty() -> "duplicate"
                else -> "error"
            }

            val message = when (status) {
                "success" -> "Attendance recorded"
                "duplicate" -> "Already checked in"
                else -> "Attendance failed"
            }

            MarkAttendanceResponseDto(
                status = status,
                message = message,
                markedPresent = toInsert,
                duplicates = existingPids.toList(),
                progress = "$totalEventAttendance/$totalParticipantsCount",
                eventAttendance = totalEventAttendance,
                totalParticipants = totalParticipantsCount
            )
        }.recoverCatching { directError ->
            // If direct fallback also failed, throw the RPC exception (or direct error)
            throw rpcAttempt.exceptionOrNull() ?: directError
        }
    }

    override fun recordScanSuccess(
        eventId: Int,
        participantId: String,
        name: String,
        teamId: String?,
        teamName: String?,
        currentTime: String,
        progress: String?,
        newAttendance: Int?,
        total: Int?
    ) {
        val currentDetail = _eventDetailsMap.value[eventId]
        val newRecord = AttendanceRecord(
            id = "${eventId}_${participantId}",
            eventId = eventId.toString(),
            participantId = participantId,
            checkedInAt = currentTime,
            status = "present",
            participantName = name,
            participantUsn = participantId,
            teamId = teamId,
            teamName = teamName,
            isPendingSync = false
        )
        val existingAttendees = currentDetail?.attendees ?: emptyList()
        val updatedAttendees = listOf(newRecord) + existingAttendees.filterNot { it.participantId == participantId }
        val updatedScanned = newAttendance ?: ((currentDetail?.scannedCount ?: 0) + 1)
        val updatedTotal = total ?: (currentDetail?.totalExpected ?: 0)
        val updatedProgress = progress ?: "$updatedScanned/$updatedTotal"

        val updatedDetail = (currentDetail ?: EventDetailData(
            eventId = eventId,
            eventName = "Event $eventId"
        )).copy(
            scannedCount = updatedScanned,
            totalExpected = updatedTotal,
            progress = updatedProgress,
            attendees = updatedAttendees
        )
        _eventDetailsMap.value = _eventDetailsMap.value + (eventId to updatedDetail)

        _eventsWithStats.value = _eventsWithStats.value.map { item ->
            if (item.event.eventId == eventId) {
                item.copy(
                    scannedCount = updatedScanned,
                    totalEligibleCount = if (updatedTotal > 0) updatedTotal else item.totalEligibleCount
                )
            } else {
                item
            }
        }
    }

    override fun recordBulkScanSuccess(
        eventId: Int,
        markedAttendees: List<AttendanceRecord>,
        progress: String?,
        newAttendance: Int?,
        total: Int?
    ) {
        val currentDetail = _eventDetailsMap.value[eventId]
        val existingAttendees = currentDetail?.attendees ?: emptyList()
        val markedIds = markedAttendees.map { it.participantId }.toSet()
        val updatedAttendees = markedAttendees + existingAttendees.filterNot { it.participantId in markedIds }
        val updatedScanned = newAttendance ?: (existingAttendees.size + markedAttendees.size)
        val updatedTotal = total ?: (currentDetail?.totalExpected ?: 0)
        val updatedProgress = progress ?: "$updatedScanned/$updatedTotal"

        val updatedDetail = (currentDetail ?: EventDetailData(
            eventId = eventId,
            eventName = "Event $eventId"
        )).copy(
            scannedCount = updatedScanned,
            totalExpected = updatedTotal,
            progress = updatedProgress,
            attendees = updatedAttendees
        )
        _eventDetailsMap.value = _eventDetailsMap.value + (eventId to updatedDetail)

        _eventsWithStats.value = _eventsWithStats.value.map { item ->
            if (item.event.eventId == eventId) {
                item.copy(
                    scannedCount = updatedScanned,
                    totalEligibleCount = if (updatedTotal > 0) updatedTotal else item.totalEligibleCount
                )
            } else {
                item
            }
        }
    }

    override suspend fun exportAttendanceCsv(eventId: String): Result<String> = withContext(ioDispatcher) {
        runCatching {
            val eId = eventId.toIntOrNull() ?: 0
            val eventDetail = _eventDetailsMap.value[eId] ?: syncEventAttendance(eId).getOrThrow()
            val builder = StringBuilder()
            builder.append("Event,Participant ID,Name,Team ID,Check-in Time,Status\n")
            for (row in eventDetail.attendees) {
                val team = row.teamName ?: row.teamId ?: ""
                builder.append("\"${eventDetail.eventName}\",\"${row.participantId}\",\"${row.participantName ?: ""}\",\"$team\",\"${row.checkedInAt}\",\"present\"\n")
            }
            builder.toString()
        }
    }

    override suspend fun clearCache(): Unit = withContext(ioDispatcher) {
        if (eventsCacheFile.exists()) {
            eventsCacheFile.delete()
        }
        _eventsWithStats.value = emptyList()
        _eventDetailsMap.value = emptyMap()
    }
}

