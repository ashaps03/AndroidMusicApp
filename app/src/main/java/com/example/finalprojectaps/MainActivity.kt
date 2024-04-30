package com.example.finalprojectaps

import PlaylistTrackResult
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val spotifyService: SpotifyService by lazy {
        createSpotifyService()
    }

    private lateinit var audioPlayer: AudioPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioPlayer = AudioPlayer.getInstance(this)


        // Call the function to fetch playlist tracks
        fetchRandomSongFromPlaylist("06wg2JXyo7yZWt73sLFQ0I")
    }

    private fun fetchRandomSongFromPlaylist(playlistId: String) {
        // Coroutine scope to perform async operations
        CoroutineScope(Dispatchers.IO).launch {
            // Get the access token
            val clientId = getString(R.string.spotify_client_id)
            val clientSecret = getString(R.string.spotify_client_secret)
            NetworkUtils.getToken(clientId, clientSecret) { accessToken ->
                accessToken?.let { token ->
                    // Make the API call using the retrieved access token
                    spotifyService.getPlaylistTracks(playlistId, "Bearer $token")
                        .enqueue(object : Callback<PlaylistTrackResult> {
                            override fun onResponse(
                                call: Call<PlaylistTrackResult>,
                                response: Response<PlaylistTrackResult>
                            ) {
                                if (response.isSuccessful) {
                                    val playlistTrackResult = response.body()
                                    // Handle the response
                                    playlistTrackResult?.items?.let { tracks ->
                                        if (tracks.isNotEmpty()) {
                                            // Pick a random track from the list
                                            val randomTrack = tracks.random().track
                                            println("Track Name: ${randomTrack.name}")
                                            println("Artist Name: ${randomTrack.artists.joinToString(", ") { it.name }}")
                                            println("Spotify URL: ${randomTrack.externalUrls.spotify}")
                                            println("Preview URL: ${randomTrack.previewUrl}")

                                            randomTrack.previewUrl?.let { previewUrl ->
                                                audioPlayer.playPreview(this@MainActivity, previewUrl)
                                            }
                                        } else {
                                            println("No tracks found for the playlist: $playlistId")
                                        }
                                    }
                                } else {
                                    // Handle unsuccessful response
                                    println("Failed to fetch playlist tracks: ${response.message()}")
                                }
                            }

                            override fun onFailure(call: Call<PlaylistTrackResult>, t: Throwable) {
                                // Handle network error
                                println("Network error: ${t.message}")
                            }
                        })
                } ?: println("Failed to retrieve access token")
            }
        }
    }

    // Retrofit service creation
    private fun createSpotifyService(): SpotifyService {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyService::class.java)
    }

}
