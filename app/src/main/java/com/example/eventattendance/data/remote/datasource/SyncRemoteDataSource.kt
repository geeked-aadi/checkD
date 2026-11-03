package com.example.eventattendance.data.remote.datasource

import com.example.eventattendance.data.remote.dto.SyncMetadataDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getSyncMetadata(key: String = "global"): SyncMetadataDto? {
        return supabaseClient.from("sync_metadata")
            .select {
                filter {
                    eq("key", key)
                }
            }
            .decodeSingleOrNull<SyncMetadataDto>()
    }
}
