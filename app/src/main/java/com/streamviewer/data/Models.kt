package com.streamviewer.data

import com.google.gson.annotations.SerializedName

// --- Login / Auth ---

data class LoginResponse(
    val success: Boolean,
    val url: String?,
    @SerializedName("user_info") val userInfo: UserInfo?,
    val error: String?
)

data class UserInfo(
    val username: String?,
    val message: String?,
    @SerializedName("auth") val authStatus: Int?,
    val status: String?,
    val exp_date: String?,
    @SerializedName("is_trial") val isTrial: String?,
    @SerializedName("active_cons") val activeCons: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("max_connections") val maxConnections: String?,
    val allowed_output_formats: List<String>?
)

data class LoginRequest(
    val username: String,
    val password: String,
    val url: String
)

// --- Categories ---

data class Category(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String, // from Xtream
    @SerializedName("parent_id") val parentId: Int?,

    // UI specific
    var name: String? = null, // Sometimes mapped differently
    var custom_name: String? = null,
    var preselected: Boolean = false
)

// --- Content / Streams ---

// The server returns a Map<String, List<StreamEntry>> for categories
data class StreamEntry(
    val added: String?,
    val cast: String?,
    val category_id: String?,
    val container_extension: String?,
    val cover: String?, // General cover
    val director: String?,
    val name: String,
    val num: Any?, // Can be string or int
    val plot: String?,
    val rating: Any?, // Can be 5.0 or "5"
    val releaseDate: String?,
    val series_id: Int?, // Series have this
    val stream_id: Int?, // Movies have this
    val stream_type: String?,
) {
    fun getId(): Int {
        return stream_id ?: series_id ?: 0
    }

    fun getImageUrl(): String {
        return cover ?: ""
    }
}

// --- Series Details ---

data class SeriesInfoResponse(
    val seasons: List<Season>,
    val episodes: Map<String, List<Episode>>,
    val error: String?
)

data class Season(
    @SerializedName("season_number") val seasonNumber: Int,
    val name: String,
    @SerializedName("episode_count") val episodeCount: Int,
    val id: Int?
)

data class Episode(
    val id: Int,
    val season: Int,
    @SerializedName("episode_num") val episodeNum: Int,
    val title: String,
    @SerializedName("container_extension") val containerExtension: String,
    val info: EpisodeInfo?,
    val movie_image: String?
)

data class EpisodeInfo(
    val movie_image: String?,
    val plot: String?,
    val duration_secs: Int?,
    val duration: String?
)

// --- Watchlist / Favorites ---

data class PlaybackEntry(
    val media_key: String,
    val name: String?,
    val position: Double?,
    val duration: Double?,
    val updated_at: String?,
    val cover: String? = null,
    val rating: Any? = null,
    val plot: String? = null,
    val cast: String? = null,
    val stream_type: String? = null,
    val stream_id: Int? = null,
    val series_id: Int? = null,
    val category_id: Int? = null,
    val container_extension: String,
    val director: String?
) {
    fun toStreamEntry(): StreamEntry {
        var sId = stream_id
        var serId = series_id

        if (sId == null && serId == null) {
            val parts = media_key.split(":")
            if (parts.size == 2) {
                val type = parts[0]
                val id = parts[1].toIntOrNull()
                if (type == "movies") {
                    sId = id
                } else if (type == "series") {
                    serId = id
                }
            }
        }

        return StreamEntry(
            stream_id = sId,
            series_id = serId,
            num = null,
            name = name ?: "",
            stream_type = stream_type,
            cover = cover,
            rating = rating,
            added = updated_at,
            category_id = category_id.toString(),
            container_extension = container_extension,
            plot = plot,
            cast = cast,
            director = director,
            releaseDate = updated_at,
        )
    }
}

data class PlaybackSaveRequest(
    val media_key: String,
    val position: Double,
    val duration: Double,
    val file: String? // e.g. "episode_id.mkv"
)

data class FavoriteRequest(
    val media_key: String
)
