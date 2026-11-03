package com.example.eventattendance.data.remote.datasource

import com.example.eventattendance.data.remote.dto.EligibilityDto
import com.example.eventattendance.data.remote.dto.ParticipantDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParticipantRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getAllParticipants(): List<ParticipantDto> {
        return supabaseClient.from("participants")
            .select(columns = Columns.ALL)
            .decodeList<ParticipantDto>()
    }

    suspend fun getAllEligibility(): List<EligibilityDto> {
        return supabaseClient.from("event_eligibility")
            .select(columns = Columns.ALL)
            .decodeList<EligibilityDto>()
    }

    suspend fun getParticipantByQrToken(qrToken: String): ParticipantDto? {
        return supabaseClient.from("participants")
            .select(columns = Columns.ALL) {
                filter {
                    eq("qr_token", qrToken)
                }
            }
            .decodeSingleOrNull<ParticipantDto>()
    }
}
