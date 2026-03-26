package dev.x341.maps.database

import kotlinx.serialization.Serializable

@Serializable
data class UserMarker(
    val id: String? = null,
    val user_id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String? = null,
    val snippet: String? = null,
    val image_url: String? = null,
    val created_at: String? = null
)
