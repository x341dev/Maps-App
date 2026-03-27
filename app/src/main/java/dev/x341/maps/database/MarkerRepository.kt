package dev.x341.maps.database

import android.graphics.Bitmap
import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.io.ByteArrayOutputStream
import java.util.UUID



class MarkerRepository {
    private val supabase = SupabaseClient.client
    private val tableName = "markers"
    private val bucketName = "marker_images"

    suspend fun uploadAndSaveMarker(
        lat: Double,
        lng: Double,
        title: String?,
        snippet: String?,
        bitmap: Bitmap
    ): Boolean  {
        return try {
            val session = supabase.auth.currentSessionOrNull()
            val userId = session?.user?.id?: throw Exception("User not authenticated")

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()

            val fileName = "marker_${UUID.randomUUID()}.jpg"
            val path = "$userId/$fileName"

            Log.d("SupabaseRepo", "Uploading image to path: $path")
            val bucket = supabase.storage[bucketName]
            bucket.upload(path, byteArray) {
                contentType = ContentType.Image.JPEG
            }

            val publicUrl = bucket.publicUrl(path)
            Log.d("SupabaseRepo", "Image uploaded, public URL: $publicUrl")

            val newMarker = UserMarker(
                user_id = userId,
                latitude = lat,
                longitude = lng,
                title = title,
                snippet = snippet,
                image_url = publicUrl
            )

            Log.d("SupabaseRepo", "Inserting marker with data: $newMarker")
            supabase.postgrest[tableName].insert(newMarker)
            Log.d("SupabaseRepo", "Marker inserted successfully")

            true
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error uploading and saving marker: ${e.message}")
            e.printStackTrace()
            false
        }
    }

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

    suspend fun updateMarker(
        markerId: String,
        lat: Double,
        lng: Double,
        title: String?,
        snippet: String?,
        bitmap: Bitmap?,
        existingImageUrl: String?
    ): Boolean {
        return try {
            val session = supabase.auth.currentSessionOrNull()
            val userId = session?.user?.id ?: throw Exception("User not authenticated")

            var finalImageUrl = existingImageUrl

            if (bitmap != null) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                val fileName = "${userId}_${UUID.randomUUID()}.jpg"

                val bucket = supabase.storage.from("markers")
                bucket.upload(fileName, imageBytes) {
                    upsert = true
                }
                finalImageUrl = bucket.publicUrl(fileName)
            }

            val updatedMarker = UserMarker(
                id = markerId,
                user_id = userId,
                latitude = lat,
                longitude = lng,
                title = title,
                snippet = snippet,
                image_url = finalImageUrl
            )

            supabase.postgrest[tableName].update(updatedMarker) {
                filter {
                    UserMarker::id eq markerId
                }
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error updating: ${e.message}")
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

    suspend fun deleteMarker(markerId: String, imageUrl: String?): Boolean {
        return try {
            if (!imageUrl.isNullOrEmpty()) {
                try {
                    val fileName = imageUrl.substringAfter("/")
                    supabase.storage.from(bucketName).delete(fileName)
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error deleting image: ${e.message}")
                }
            }

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