package com.example.eventattendance.data.repository

import android.content.Context
import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource
import com.example.eventattendance.data.remote.dto.EventAttendanceItemDto
import com.example.eventattendance.data.remote.dto.MarkAttendanceResponseDto
import com.example.eventattendance.data.remote.dto.SyncAllEventsAttendanceResponseDto
import com.example.eventattendance.di.IoDispatcher
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
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val rpcRemoteDataSource: RpcRemoteDataSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AttendanceRepository {

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
        _eventsWithStats.value = mappedEvents

        // Also pre-populate event details
        val detailsMap = _eventDetailsMap.value.toMutableMap()
        for (item in events) {
            val attendees = item.attendanceDetails.map { detail ->
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
            if (response.events.isNotEmpty()) {
                val jsonString = json.encodeToString(response)
                eventsCacheFile.writeText(jsonString)
                mapAndEmitEvents(response.events)
            }
            _eventsWithStats.value
        }.onFailure {
            // Offline fallback: load cached data if present
            if (_eventsWithStats.value.isEmpty()) {
                loadCachedEvents()
            }
        }
    }

    override suspend fun syncEventAttendance(eventId: Int): Result<EventDetailData> = withContext(ioDispatcher) {
        runCatching {
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
    }

    override fun getEventAttendanceFlow(eventId: Int): Flow<EventDetailData?> {
        return _eventDetailsMap.map { it[eventId] }
    }

    override suspend fun markAttendance(
        eventId: Int,
        pIds: List<String>,
        role: String
    ): Result<MarkAttendanceResponseDto> = withContext(ioDispatcher) {
        runCatching {
            rpcRemoteDataSource.markAttendance(eventId, pIds, role)
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
}
