package com.streamviewer.api

import com.streamviewer.data.*
import retrofit2.Response
import retrofit2.http.*

interface StreamViewerApi {

    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/api/load-categories")
    suspend fun loadCategories(
        @Query("type") type: String // "movies" or "series"
    ): Response<Map<String, List<StreamEntry>>>

    @POST("/api/get-movie-info")
    suspend fun getMovieInfo(@Body body: Map<String, Int>): Response<Map<String, String?>>
    // body: { "stream_id": 123 }
    // returns: { "plot": "...", "cast": "...", ... }

    @POST("/api/get-series-info")
    suspend fun getSeriesInfo(@Body body: Map<String, Int>): Response<SeriesInfoResponse>
    // body: { "series_id": 123 }

    // Playback / Watchlist
    @GET("/api/playback/list")
    suspend fun getWatchlist(@Query("type") type: String): Response<List<PlaybackEntry>>

    @POST("/api/playback/save")
    suspend fun savePlayback(@Body request: PlaybackSaveRequest): Response<Map<String, Boolean>>

    @GET("/api/playback")
    suspend fun getPlaybackPosition(@Query("media_key") mediaKey: String): Response<Map<String, Any>>
    // Returns { success: true, entry: { position: 123.0, ... } }

    // Favorites
    @GET("/api/favorites/list")
    suspend fun getFavorites(@Query("type") type: String): Response<List<PlaybackEntry>> // Reusing PlaybackEntry structure as it's similar

    @POST("/api/favorites/save")
    suspend fun addFavorite(@Body request: FavoriteRequest): Response<Map<String, Boolean>>

    @GET("/api/remove-favorite")
    suspend fun removeFavorite(@Query("media_key") mediaKey: String): Response<Map<String, Boolean>>

    @GET("/api/remove-playback")
    suspend fun removePlayback(@Query("media_key") mediaKey: String): Response<Map<String, Boolean>>

    // Category Sync
    @GET("/api/get-all-categories")
    suspend fun getAllCategories(@Query("type") type: String): Response<List<Category>>

    @POST("/api/set-categories")
    suspend fun setCategories(
        @Query("type") type: String,
        @Body categories: List<Category>
    ): Response<Map<String, Boolean>>
}
