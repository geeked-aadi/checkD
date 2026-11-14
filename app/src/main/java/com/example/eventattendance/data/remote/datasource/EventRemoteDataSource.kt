package com.example.eventattendance.data.remote.datasource

import com.example.eventattendance.domain.calculator.ProgressCalculator
import com.example.eventattendance.data.remote.dto.EventDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CreateEventDto(
    @SerialName("event_name")
    val eventName: String,
    @SerialName("starts_from")
    val startsFrom: String?,
    @SerialName("ends_on")
    val endsOn: String?
)

@Singleton
class EventRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getAllEvents(): List<EventDto> {
        val events = supabaseClient.from("events")
            .select(columns = Columns.ALL)
            .decodeList<EventDto>()
        return ProgressCalculator.sortEventsByTime(
            events = events,
            startsFrom = { it.startsFrom },
            endsOn = { it.endsOn },
            eventId = { it.eventId }
        )
    }

    suspend fun getEventById(eventId: Int): EventDto? {
        return supabaseClient.from("events")
            .select(columns = Columns.ALL) {
                filter {
                    eq("event_id", eventId)
                }
            }
            .decodeSingleOrNull<EventDto>()
    }

    suspend fun createEvent(
        eventName: String,
        startsFrom: String,
        endsOn: String
    ): EventDto {
        val cleanStart = startsFrom.trim().replace(' ', 'T')
        val cleanEnd = endsOn.trim().replace(' ', 'T')
        val cleanName = eventName.trim().ifBlank { "New Event" }

        // 1. Direct table insert (does not supply event_id, allowing PostgreSQL GENERATED ALWAYS AS IDENTITY to assign it)
        val directResult = runCatching {
            val createDto = CreateEventDto(
                eventName = cleanName,
                startsFrom = cleanStart,
                endsOn = cleanEnd
            )
            supabaseClient.from("events")
                .insert(createDto) {
                    select()
                }
                .decodeSingle<EventDto>()
        }
        if (directResult.isSuccess) {
            return directResult.getOrThrow()
        }

        // 2. RPC fallback
        val rpcResult = runCatching {
            val params = buildJsonObject {
                put("p_event_name", cleanName)
                cleanStart.takeIf { it.isNotBlank() }?.let { put("p_starts_from", it) }
                cleanEnd.takeIf { it.isNotBlank() }?.let { put("p_ends_on", it) }
            }
            supabaseClient.postgrest.rpc("create_event", params)
            getAllEvents().maxByOrNull { it.eventId }
                ?: EventDto(eventId = 0, eventName = cleanName, startsFrom = cleanStart, endsOn = cleanEnd)
        }
        if (rpcResult.isSuccess) {
            return rpcResult.getOrThrow()
        }

        throw directResult.exceptionOrNull() ?: rpcResult.exceptionOrNull()
            ?: Exception("Failed to create event in database")
    }

    suspend fun createEvent(eventDto: EventDto): EventDto {
        return createEvent(
            eventName = eventDto.eventName ?: "New Event",
            startsFrom = eventDto.startsFrom ?: "",
            endsOn = eventDto.endsOn ?: ""
        )
    }

    suspend fun getNextEventId(): Int {
        val events = getAllEvents()
        return (events.maxOfOrNull { it.eventId } ?: 0) + 1
    }

    suspend fun updateEventTimeWindow(
        eventId: Int,
        startsFrom: String,
        endsOn: String,
        name: String? = null
    ) {
        val cleanStart = startsFrom.trim().replace(' ', 'T')
        val cleanEnd = endsOn.trim().replace(' ', 'T')
        val cleanName = name?.trim()?.takeIf { it.isNotBlank() }

        // 1. Primary: Direct table update via PostgREST PATCH
        val directResult = runCatching {
            val updatedRows = supabaseClient.from("events").update(
                {
                    if (cleanName != null) set("event_name", cleanName)
                    set("starts_from", cleanStart)
                    set("ends_on", cleanEnd)
                }
            ) {
                filter {
                    eq("event_id", eventId)
                }
                select()
            }.decodeList<EventDto>()

            if (updatedRows.isEmpty()) {
                throw Exception("No event found with id $eventId")
            }
            Unit
        }
        if (directResult.isSuccess) {
            return
        }

        // 2. Fallback: RPC update_event_time_window
        // Always provide p_event_name (even empty) so PostgREST avoids PGRST203 overloading conflict
        val rpcResult = runCatching {
            val params = buildJsonObject {
                put("p_event_id", eventId)
                put("p_starts_from", cleanStart)
                put("p_ends_on", cleanEnd)
                put("p_event_name", cleanName ?: "")
            }
            supabaseClient.postgrest.rpc("update_event_time_window", params)
            Unit
        }
        if (rpcResult.isSuccess) {
            return
        }

        throw directResult.exceptionOrNull() ?: rpcResult.exceptionOrNull()
            ?: Exception("Failed to update event $eventId in database")
    }
}
