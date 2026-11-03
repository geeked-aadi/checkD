package com.example.eventattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventattendance.data.local.entity.SyncMetaEntity

@Dao
interface SyncMetaDao {

    @Query("SELECT * FROM sync_meta WHERE `key` = :key LIMIT 1")
    suspend fun getSyncMeta(key: String = "last_sync"): SyncMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSyncMeta(meta: SyncMetaEntity)

    @Query("DELETE FROM sync_meta")
    suspend fun clear()
}
