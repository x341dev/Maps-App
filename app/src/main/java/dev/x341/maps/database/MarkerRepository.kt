package dev.x341.maps.database

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
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
        Log.d("SupabaseRepo", "Starting uploadAndSaveMarker execution")
        return try {
            val session = supabase.auth.currentSessionOrNull()
            val userId = session?.user?.id?: throw Exception("User not authenticated")
            Log.d("SupabaseRepo", "User authenticated with ID: $userId")

            Log.d("SupabaseRepo", "Compressing bitmap to JPEG")
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()
            Log.d("SupabaseRepo", "Bitmap compressed successfully, size: ${byteArray.size} bytes")

            val fileName = "marker_${UUID.randomUUID()}.jpg"
            val path = "$userId/$fileName"

            Log.d("SupabaseRepo", "Uploading image to storage bucket '$bucketName', path: $path")
            val bucket = supabase.storage[bucketName]
            bucket.upload(path, byteArray) {
                contentType = ContentType.Image.JPEG
            }

            val publicUrl = bucket.publicUrl(path)
            Log.d("SupabaseRepo", "Image uploaded successfully, public URL: $publicUrl")

            val newMarker = UserMarker(
                user_id = userId,
                latitude = lat,
                longitude = lng,
                title = title,
                snippet = snippet,
                image_url = publicUrl
            )

            Log.d("SupabaseRepo", "Inserting marker into database table '$tableName'")
            supabase.postgrest[tableName].insert(newMarker)
            Log.d("SupabaseRepo", "Marker inserted successfully into database")

            true
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error uploading and saving marker: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun insertMarker(lat: Double, lng: Double, title: String?, snippet: String?): Boolean {
        Log.d("SupabaseRepo", "Starting insertMarker execution (no image)")
        return try {
            val session = supabase.auth.currentSessionOrNull()
            val userId = session?.user?.id?: throw Exception("User not authenticated")
            Log.d("SupabaseRepo", "User authenticated with ID: $userId")

            val newMarker = UserMarker(
                user_id = userId,
                latitude = lat,
                longitude = lng,
                title = title,
                snippet = snippet
            )

            Log.d("SupabaseRepo", "Inserting marker data into table '$tableName'")
            supabase.postgrest[tableName].insert(newMarker)
            Log.d("SupabaseRepo", "Marker inserted successfully")
            true
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error inserting marker: ${e.message}")
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
        Log.d("SupabaseRepo", "Starting updateMarker execution for markerId: $markerId")
        return try {
            val session = supabase.auth.currentSessionOrNull()
            val userId = session?.user?.id ?: throw Exception("User not authenticated")
            Log.d("SupabaseRepo", "User authenticated with ID: $userId")

            var finalImageUrl = existingImageUrl

            if (bitmap != null) {
                Log.d("SupabaseRepo", "New image provided, compressing bitmap")
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()
                Log.d("SupabaseRepo", "Bitmap compressed, size: ${imageBytes.size} bytes")

                val fileName = "${userId}_${UUID.randomUUID()}.jpg"

                Log.d("SupabaseRepo", "Uploading new image to storage bucket 'markers', fileName: $fileName")
                val bucket = supabase.storage.from("markers")
                bucket.upload(fileName, imageBytes) {
                    upsert = true
                }
                finalImageUrl = bucket.publicUrl(fileName)
                Log.d("SupabaseRepo", "New image uploaded successfully, new public URL: $finalImageUrl")
            } else {
                Log.d("SupabaseRepo", "No new image provided, keeping existing image URL: $finalImageUrl")
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

            Log.d("SupabaseRepo", "Updating marker in database table '$tableName'")
            supabase.postgrest[tableName].update(updatedMarker) {
                filter {
                    UserMarker::id eq markerId
                }
            }
            Log.d("SupabaseRepo", "Marker updated successfully in database")
            true
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error updating marker: ${e.message}")
            false
        }
    }

    suspend fun getMarkers(): List<UserMarker> {
        Log.d("SupabaseRepo", "Starting getMarkers execution")
        return try {
            Log.d("SupabaseRepo", "Fetching markers from table '$tableName'")
            val markers = supabase.postgrest[tableName]
                .select()
                .decodeList<UserMarker>()

            Log.d("SupabaseRepo", "Successfully retrieved ${markers.size} markers from database")
            markers
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error retrieving markers: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteMarker(markerId: String, imageUrl: String?): Boolean {
        Log.d("SupabaseRepo", "Starting deleteMarker execution for markerId: $markerId")
        return try {
            if (!imageUrl.isNullOrEmpty()) {
                Log.d("SupabaseRepo", "Image URL found: $imageUrl. Attempting to delete from storage")
                try {
                    val bucketPrefix = "$bucketName/"
                    val filePath = if (imageUrl.contains(bucketPrefix)) {
                        imageUrl.substringAfter(bucketPrefix)
                    } else {
                        imageUrl.substringAfterLast("/")
                    }
                    Log.d("SupabaseRepo", "Extracted filename for deletion: $filePath")

                    supabase.storage.from(bucketName).delete(filePath)
                    Log.d("SupabaseRepo", "Image deleted successfully from storage bucket '$bucketName'")
                } catch (e: Exception) {
                    Log.e("SupabaseRepo", "Error deleting image from storage: ${e.message}")
                }
            } else {
                Log.d("SupabaseRepo", "No image URL associated with this marker, skipping storage deletion")
            }

            Log.d("SupabaseRepo", "Deleting marker row from database table '$tableName'")
            supabase.postgrest[tableName].delete {
                filter {
                    UserMarker::id eq markerId
                }
            }
            Log.d("SupabaseRepo", "Marker row deleted successfully from database")
            true
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Error deleting marker from database: ${e.message}")
            false
        }
    }
}