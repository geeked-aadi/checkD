package com.example.eventattendance.domain.usecase

import com.example.eventattendance.data.remote.dto.MarkAttendanceResponseDto
import com.example.eventattendance.domain.model.AttendanceMatrix
import com.example.eventattendance.domain.model.AttendanceRecord
import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.domain.model.EventDetailData
import com.example.eventattendance.domain.model.EventWithStats
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository
import com.example.eventattendance.domain.repository.AttendanceRepository
import com.example.eventattendance.domain.repository.ParticipantCacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeParticipantCacheRepository : ParticipantCacheRepository {
    val map = mutableMapOf<String, CachedParticipant>()
    val flow = MutableStateFlow<Map<String, CachedParticipant>>(emptyMap())
    var refreshCalls = 0
    var clearCalls = 0

    override val participantsFlow: StateFlow<Map<String, CachedParticipant>> = flow

    override suspend fun initializeCache() {
        refreshParticipants()
    }

    override suspend fun refreshParticipants(): Result<Unit> {
        refreshCalls++
        flow.value = map
        return Result.success(Unit)
    }

    override fun getParticipant(pId: String): CachedParticipant? = map[pId]
    override fun getAllParticipants(): List<CachedParticipant> = map.values.toList()
    override fun getByTeam(teamId: String): List<CachedParticipant> = map.values.filter { it.teamId == teamId }
    override fun getBySpoc(spocId: String): List<CachedParticipant> = map.values.filter { it.spocId == spocId }
    override fun getMemberPIds(spocId: String): Set<String> = getBySpoc(spocId).map { it.pId }.toSet()
    override fun isCacheEmpty(): Boolean = map.isEmpty()

    override suspend fun clearCache() {
        clearCalls++
        map.clear()
        flow.value = emptyMap()
    }
}

class FakeAttendanceRepository : AttendanceRepository {
    var clearCalls = 0
    var syncCalls = 0
    val eventsFlow = MutableStateFlow<List<EventWithStats>>(emptyList())

    override val eventsWithStatsFlow: StateFlow<List<EventWithStats>> = eventsFlow

    override suspend fun syncAllEventsAttendance(forcePull: Boolean): Result<List<EventWithStats>> {
        syncCalls++
        return Result.success(emptyList())
    }

    override suspend fun syncEventAttendance(eventId: Int): Result<EventDetailData> =
        Result.success(EventDetailData(eventId = eventId, eventName = "Event"))

    override fun getEventAttendanceFlow(eventId: Int): Flow<EventDetailData?> = flowOf(null)

    var lastUpdatedEventId: Int? = null
    var lastUpdatedName: String? = null
    var lastUpdatedStartsFrom: String? = null
    var lastUpdatedEndsOn: String? = null

    override fun updateLocalEvent(eventId: Int, name: String?, startsFrom: String, endsOn: String) {
        lastUpdatedEventId = eventId
        lastUpdatedName = name
        lastUpdatedStartsFrom = startsFrom
        lastUpdatedEndsOn = endsOn
    }

    override fun updateLocalEventTimeWindow(eventId: Int, name: String?, startsFrom: String, endsOn: String) {
        updateLocalEvent(eventId, name, startsFrom, endsOn)
    }

    override suspend fun markAttendance(
        eventId: Int,
        pIds: List<String>,
        role: String
    ): Result<MarkAttendanceResponseDto> = Result.success(MarkAttendanceResponseDto())

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
    ) {}

    override fun recordBulkScanSuccess(
        eventId: Int,
        markedAttendees: List<AttendanceRecord>,
        progress: String?,
        newAttendance: Int?,
        total: Int?
    ) {}

    override suspend fun exportAttendanceCsv(eventId: String): Result<String> = Result.success("")

    override suspend fun clearCache() {
        clearCalls++
        eventsFlow.value = emptyList()
    }
}

class FakeAttendanceMatrixRepository : AttendanceMatrixRepository {
    var clearCalls = 0
    var refreshCalls = 0
    var lastUpdatedEventId: Int? = null
    var lastUpdatedName: String? = null
    var lastUpdatedStartsFrom: String? = null
    var lastUpdatedEndsOn: String? = null
    val mFlow = MutableStateFlow<AttendanceMatrix?>(null)
    val uFlow = MutableStateFlow<Long?>(null)

    override val matrixFlow: StateFlow<AttendanceMatrix?> = mFlow
    override val lastUpdated: StateFlow<Long?> = uFlow

    override suspend fun refresh(): Result<Unit> {
        refreshCalls++
        return Result.success(Unit)
    }

    override fun addLocalScan(eventId: Int, pId: String) {}

    override fun updateLocalEvent(eventId: Int, name: String?, startsFrom: String, endsOn: String) {
        lastUpdatedEventId = eventId
        lastUpdatedName = name
        lastUpdatedStartsFrom = startsFrom
        lastUpdatedEndsOn = endsOn
    }

    override fun addLocalEvent(event: com.example.eventattendance.domain.model.EventAttendance) {}

    override fun clearMatrix() {
        clearCalls++
        mFlow.value = null
        uFlow.value = null
    }
}

class VerifierAndCacheTest {

    @Test
    fun testVerifyParticipantFound() = runBlocking {
        val repo = FakeParticipantCacheRepository()
        repo.map["P101"] = CachedParticipant(
            pId = "P101",
            name = "Aditya Sharma",
            teamId = "TEAM_A",
            teamName = "CodeCraft"
        )

        val useCase = VerifyParticipantUseCase(repo)
        val result = useCase("P101")
        assertNotNull(result)
        assertEquals("Aditya Sharma", result?.name)
        assertEquals("CodeCraft", result?.teamName)
    }

    @Test
    fun testVerifyParticipantNotFound() = runBlocking {
        val repo = FakeParticipantCacheRepository()
        val useCase = VerifyParticipantUseCase(repo)
        val result = useCase("NON_EXISTENT_ID")
        assertNull(result)
    }

    @Test
    fun testClearLocalCacheUseCasePurgesAndResyncs() = runBlocking {
        val pRepo = FakeParticipantCacheRepository()
        val aRepo = FakeAttendanceRepository()
        val mRepo = FakeAttendanceMatrixRepository()

        pRepo.map["P101"] = CachedParticipant(pId = "P101", name = "Aditya")

        val clearUseCase = ClearLocalCacheUseCase(pRepo, aRepo, mRepo)
        val result = clearUseCase()

        assertTrue(result.isSuccess)
        assertEquals(1, pRepo.clearCalls)
        assertEquals(1, aRepo.clearCalls)
        assertEquals(1, mRepo.clearCalls)
        assertEquals(1, pRepo.refreshCalls)
        assertEquals(1, aRepo.syncCalls)
        assertEquals(1, mRepo.refreshCalls)
    }

    @Test
    fun testUpdateLocalEventUpdatesAttendanceAndMatrixFakes() {
        val aRepo = FakeAttendanceRepository()
        val mRepo = FakeAttendanceMatrixRepository()

        aRepo.updateLocalEvent(
            eventId = 42,
            name = "Hackathon Finals",
            startsFrom = "2026-11-15T09:00:00",
            endsOn = "2026-11-15T18:00:00"
        )
        mRepo.updateLocalEvent(
            eventId = 42,
            name = "Hackathon Finals",
            startsFrom = "2026-11-15T09:00:00",
            endsOn = "2026-11-15T18:00:00"
        )

        assertEquals(42, aRepo.lastUpdatedEventId)
        assertEquals("Hackathon Finals", aRepo.lastUpdatedName)
        assertEquals("2026-11-15T09:00:00", aRepo.lastUpdatedStartsFrom)
        assertEquals("2026-11-15T18:00:00", aRepo.lastUpdatedEndsOn)

        assertEquals(42, mRepo.lastUpdatedEventId)
        assertEquals("Hackathon Finals", mRepo.lastUpdatedName)
        assertEquals("2026-11-15T09:00:00", mRepo.lastUpdatedStartsFrom)
        assertEquals("2026-11-15T18:00:00", mRepo.lastUpdatedEndsOn)
    }
}
