package com.example.finalprojectaps

import PlaylistTrackResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface SpotifyService {
    @GET("v1/playlists/{playlist_id}/tracks")
    fun getPlaylistTracks(
        @Path("playlist_id") playlistId: String,
        @Header("Authorization") authorization: String
    ): Call<PlaylistTrackResult>
}

