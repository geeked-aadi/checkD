package com.example.eventattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventattendance.data.local.entity.ParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {

    @Query("SELECT * FROM participants WHERE qrToken = :qrToken LIMIT 1")
    suspend fun getParticipantByQrToken(qrToken: String): ParticipantEntity?

    @Query("SELECT * FROM participants WHERE id = :id LIMIT 1")
    suspend fun getParticipantById(id: String): ParticipantEntity?

    @Query("SELECT * FROM participants ORDER BY name ASC")
    fun getAllParticipantsFlow(): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants ORDER BY name ASC")
    suspend fun getAllParticipants(): List<ParticipantEntity>

    @Query("""
        SELECT * FROM participants 
        WHERE name LIKE '%' || :query || '%' OR usn LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchParticipants(query: String): Flow<List<ParticipantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<ParticipantEntity>)

    @Query("DELETE FROM participants")
    suspend fun clearAll()
}
