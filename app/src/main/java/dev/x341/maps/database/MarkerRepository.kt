package dev.x341.maps.database

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest


class MarkerRepository {
    private val supabase = SupabaseClient.client
    private val tableName = "markers"

    suspend fun insertMarker(lat: Double, lng: Double, title: String?, snippet: String?): Boolean {
        return try {
            val session = supabase.auth.currentSessionOrNull()
            val userId = session?.user?.id?: throw Exception("User not authenticated")

            val newMarker = UserMarker(
                user_id = userId,
                latitude = lat,
                longitude = lng,
                title = title,
                snippet = snippet
            )

            supabase.postgrest[tableName].insert(newMarker)
            true
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error inserting: ${e.message}")
            false
        }
    }

    suspend fun getMarkers(): List<UserMarker> {
        return try {
            supabase.postgrest[tableName]
                .select()
                .decodeList<UserMarker>()
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error retrieving markers: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteMarker(markerId: String): Boolean {
        return try {
            supabase.postgrest[tableName].delete {
                filter {
                    UserMarker::id eq markerId
                }
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error deleting: ${e.message}")
            false
        }
    }
}