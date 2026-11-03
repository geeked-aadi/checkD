package com.example.eventattendance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_meta")
data class SyncMetaEntity(
    @PrimaryKey
    val key: String = "last_sync",
    val cloudTimestamp: String,
    val localSyncedAt: Long = System.currentTimeMillis()
)
