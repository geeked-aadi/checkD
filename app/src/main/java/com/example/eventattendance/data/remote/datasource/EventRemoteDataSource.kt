package com.example.eventattendance.data.remote.datasource

import com.example.eventattendance.data.remote.dto.EventDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getAllEvents(): List<EventDto> {
        return supabaseClient.from("events")
            .select(columns = Columns.ALL)
            .decodeList<EventDto>()
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

    suspend fun createEvent(eventDto: EventDto): EventDto {
        return supabaseClient.from("events")
            .insert(eventDto) {
                select()
            }
            .decodeSingle<EventDto>()
    }

    suspend fun getNextEventId(): Int {
        val events = getAllEvents()
        return (events.maxOfOrNull { it.eventId } ?: 0) + 1
    }

    suspend fun updateEventTimeWindow(eventId: Int, startsFrom: String, endsOn: String) {
        supabaseClient.from("events").update(
            {
                set("starts_from", startsFrom)
                set("ends_on", endsOn)
            }
        ) {
            filter {
                eq("event_id", eventId)
            }
        }
    }
}
