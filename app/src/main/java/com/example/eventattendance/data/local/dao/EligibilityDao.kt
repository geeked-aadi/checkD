package com.example.eventattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventattendance.data.local.entity.EventEligibilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EligibilityDao {

    @Query("SELECT COUNT(*) FROM event_eligibility WHERE eventId = :eventId")
    fun getEligibleCountFlow(eventId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM event_eligibility WHERE eventId = :eventId")
    suspend fun getEligibleCount(eventId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM event_eligibility WHERE eventId = :eventId AND participantId = :participantId)")
    suspend fun isParticipantEligible(eventId: String, participantId: String): Boolean

    @Query("SELECT participantId FROM event_eligibility WHERE eventId = :eventId")
    suspend fun getEligibleParticipantIds(eventId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEligibility(items: List<EventEligibilityEntity>)

    @Query("DELETE FROM event_eligibility")
    suspend fun clearAll()
}
