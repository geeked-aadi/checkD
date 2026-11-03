package com.example.eventattendance.data.remote.datasource

import com.example.eventattendance.data.remote.dto.AttendanceRecordDto
import com.example.eventattendance.data.remote.dto.BulkAttendanceRpcResponse
import com.example.eventattendance.data.remote.dto.SingleAttendanceRpcResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getAllAttendanceRecords(): List<AttendanceRecordDto> {
        return supabaseClient.from("attendance_records")
            .select(columns = Columns.ALL)
            .decodeList<AttendanceRecordDto>()
    }

    suspend fun markAttendance(eventId: String, qrToken: String): SingleAttendanceRpcResponse {
        val params = buildJsonObject {
            put("p_event_id", JsonPrimitive(eventId))
            put("p_qr_token", JsonPrimitive(qrToken))
        }

        return supabaseClient.postgrest.rpc(
            function = "mark_attendance",
            parameters = params
        ).decodeAs<SingleAttendanceRpcResponse>()
    }

    suspend fun markBulkAttendance(eventId: String, participantIds: List<String>): BulkAttendanceRpcResponse {
        val params = buildJsonObject {
            put("p_event_id", JsonPrimitive(eventId))
            put("p_student_ids", buildJsonArray {
                participantIds.forEach { add(JsonPrimitive(it)) }
            })
        }

        return supabaseClient.postgrest.rpc(
            function = "mark_bulk_attendance",
            parameters = params
        ).decodeAs<BulkAttendanceRpcResponse>()
    }
}
