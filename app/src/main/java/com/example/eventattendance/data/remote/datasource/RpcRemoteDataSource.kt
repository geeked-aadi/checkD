package com.example.eventattendance.data.remote.datasource

import com.example.eventattendance.data.remote.dto.AttendanceMatrixResponseDto
import com.example.eventattendance.data.remote.dto.GetAllParticipantsResponseDto
import com.example.eventattendance.data.remote.dto.MarkAttendanceResponseDto
import com.example.eventattendance.data.remote.dto.SyncAllEventsAttendanceResponseDto
import com.example.eventattendance.data.remote.dto.SyncEventAttendanceResponseDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RpcRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun getAllParticipants(): GetAllParticipantsResponseDto {
        val result = supabaseClient.postgrest.rpc("get_all_participants")
        return json.decodeFromString<GetAllParticipantsResponseDto>(result.data)
    }

    suspend fun syncAllEventsAttendance(): SyncAllEventsAttendanceResponseDto {
        val result = supabaseClient.postgrest.rpc("sync_all_events_attendance")
        return json.decodeFromString<SyncAllEventsAttendanceResponseDto>(result.data)
    }

    suspend fun syncEventAttendance(eventId: Int): SyncEventAttendanceResponseDto {
        val params = buildJsonObject {
            put("p_event_id", eventId)
        }
        val result = supabaseClient.postgrest.rpc("sync_event_attendance", params)
        return json.decodeFromString<SyncEventAttendanceResponseDto>(result.data)
    }

    suspend fun markAttendance(eventId: Int, pIds: List<String>, role: String): MarkAttendanceResponseDto {
        val params = buildJsonObject {
            put("p_event_id", eventId)
            put("p_p_ids", buildJsonArray {
                pIds.forEach { add(JsonPrimitive(it)) }
            })
            put("p_role", role)
        }
        val result = supabaseClient.postgrest.rpc("mark_attendance", params)
        return json.decodeFromString<MarkAttendanceResponseDto>(result.data)
    }

    suspend fun getAttendanceMatrix(): AttendanceMatrixResponseDto {
        val result = supabaseClient.postgrest.rpc("get_attendance_matrix")
        return json.decodeFromString<AttendanceMatrixResponseDto>(result.data)
    }
}
