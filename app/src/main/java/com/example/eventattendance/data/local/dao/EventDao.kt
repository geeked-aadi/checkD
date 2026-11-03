package com.example.eventattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventattendance.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getEventByIdFlow(id: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("DELETE FROM events")
    suspend fun clearAll()
}
