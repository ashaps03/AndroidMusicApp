package com.example.finalprojectaps

import PlaylistTrackResult
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.finalprojectaps.databinding.ActivityMainBinding
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var audioPlayer: AudioPlayer
    private var currentSongName: String = ""
    private lateinit var songName: String // Declare songName as a class-level variable
    private lateinit var songNames: String // Declare songName as a class-level variable

    private lateinit var hint: String // Declare songName as a class-level variable
    private lateinit var myAdapter: MyAdapter
    private var myDataSet: Array<String> = arrayOf()

    private var hintShown: Boolean = false

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private val sensorListener = object : SensorEventListener {
        private var lastUpdate: Long = 0
        private var last_x: Float = 0.0f
        private var last_y: Float = 0.0f
        private var last_z: Float = 0.0f
        private val SHAKE_THRESHOLD = 5

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // This can be left empty unless you have a specific use case
        }

        override fun onSensorChanged(event: SensorEvent) {
            val curTime = System.currentTimeMillis()
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                val diffTime = curTime - lastUpdate
                lastUpdate = curTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

                if (speed > SHAKE_THRESHOLD) {
                    pass() // call your method to pass the song
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Song passed!", Toast.LENGTH_SHORT).show()
                    }
                }


                last_x = x
                last_y = y
                last_z = z
            }
        }
    }





    private lateinit var timer: CountDownTimer
    private val TIMER_DURATION: Long = 60000 // 1 minute in milliseconds
    private var score: Int = 0 // Variable to keep track of the score


    private val spotifyService: SpotifyService by lazy {
        createSpotifyService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recycle.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty dataset
        myDataSet = arrayOf()
        myAdapter = MyAdapter(myDataSet)
        binding.recycle.adapter = myAdapter

        binding.answerQTextview.isEnabled = true
        binding.answerButton.isEnabled = true

        audioPlayer = AudioPlayer.getInstance(this)

        // Apply animation to the ImageView
        val animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        binding.imageView.startAnimation(animation)


        //sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        // Call the function to fetch playlist tracks
        fetchRandomSongFromPlaylist("37i9dQZF1DX1JDoW1OkYS7")

        binding.hintButton.setOnClickListener {
            // Check if hint has already been shown
            if (!hintShown) {
                toggleHintVisibility()
                hintShown = true // Set the flag to true indicating that hint has been shown

                // Disable the hint button after it's clicked
                binding.hintButton.isEnabled = false
            } else {
                showToast("Hint already shown for this song.")
            }
        }


        binding.answerButton.setOnClickListener {
            checkAnswer()
        }

        binding.passButton.setOnClickListener {
            pass()
        }
        startTimer()
        startTimer()

        binding.hintButton.setOnClickListener {
            toggleHintVisibility()
        }

    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
    }


    private var currentHint: String = ""


    private fun generateHint(songName: String): String {
        // If the current hint is already generated, return it
        if (currentHint.isNotEmpty()) {
            return currentHint
        }

        val hintLength = songName.length
        val hintStringBuilder = StringBuilder()

        // Append three random letters from the song name
        val randomIndices = songName.indices.shuffled().take(3)
        for (i in 0 until hintLength) {
            if (i in randomIndices) {
                hintStringBuilder.append(songName[i])
            } else {
                hintStringBuilder.append("_")
            }
        }

        // Store the generated hint
        currentHint = hintStringBuilder.toString()

        return currentHint
    }


    private fun toggleHintVisibility() {
        if (binding.recycle.visibility == View.VISIBLE) {
            binding.recycle.visibility = View.GONE
        } else {
            binding.recycle.visibility = View.VISIBLE

            // Generate hint based on the song name
            hint = generateHint(songName)

            // Update the adapter with the new hint
            myAdapter.updateHint(hint)
            binding.hintButton.isEnabled = false

        }
    }

    private fun resetHintState() {
        hintShown = false // Reset the hint shown flag
        binding.recycle.visibility = View.GONE // Ensure hints are not visible
        binding.hintButton.isEnabled = true // Enable hint button for new song
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkAnswer() {
        val userAnswer = binding.answerQTextview.text.toString().trim()
        val cleanUserAnswer = removeSpecialCharacters(userAnswer)
        val cleanSongName = removeSpecialCharacters(songName)

        println("User's input: $userAnswer") // Output user's input to console
        val randomTrack = // Assuming you have access to the randomTrack variable here

            if (cleanUserAnswer.equals(cleanSongName, ignoreCase = true)) {
            // Correct answer
            // Do something here, like show a toast message or navigate to the next activity
                showToast("Correct answer!")
                binding.answerQTextview.text.clear()
                score += 5
                updateScoreText()
                fetchRandomSongFromPlaylist("37i9dQZF1DX1JDoW1OkYS7")
                resetHintState() // Call this to reset hint state after correct answer



            } else {
            // Incorrect answer
            // Do something here, like show a toast message
            showToast("Incorrect answer! Try again.")
            binding.answerQTextview.text.clear()


                if (score > 0) {
                    score -= 5
                    updateScoreText()
                } else {
                    showToast("Error occurred.")

                }
            }
    }




    private fun pass() {
        // Fetch a new random song
        fetchRandomSongFromPlaylist("37i9dQZF1DX1JDoW1OkYS7")
        binding.recycle.visibility = View.GONE
        binding.hintButton.isEnabled = true

        hint = generateHint(songName)
        myAdapter.updateHint(hint)

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
                                            songName = randomTrack.name
                                            hint = songName // Update the hint variable with the new song name
                                            hintShown = false // Reset hint shown flag
                                            currentHint = "" // Reset current hint

                                            println(songName) // Print songName
                                            println(randomTrack.name)
                                            println("Track Name: ${randomTrack.name}")
                                            println("Artist Name: ${randomTrack.artists.joinToString(", ") { it.name }}")
                                            println("Spotify URL: ${randomTrack.externalUrls.spotify}")
                                            println("Preview URL: ${randomTrack.previewUrl}")

                                            val trackNames = tracks.map { it.track.name }.toTypedArray()
                                            runOnUiThread {
                                                myDataSet = trackNames
                                                myAdapter.updateData(myDataSet, hint) // Update the dataset and hint in the adapter
                                            }




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

    private fun startTimer() {
        // Cancel the previous timer if it exists
        if (::timer.isInitialized) {
            timer.cancel()
        }

        timer = object : CountDownTimer(TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((TIMER_DURATION - millisUntilFinished) / 1000).toInt()
                binding.progressBar.progress = progress
            }

            override fun onFinish() {
                showToast("Time's up!")
                audioPlayer.stop()  // Stop the audio playback
                binding.imageView.clearAnimation()  // Stop the animation on the ImageView
                binding.answerQTextview.isEnabled = false
                binding.answerQTextview.text.clear()
                binding.answerButton.isEnabled = false

                // Timer finished, do something if needed
            }
        }

        timer.start()
    }

    private fun updateScoreText() {
        binding.scoreTrack.text = "Score: $score"
    }

    private fun removeSpecialCharacters(input: String): String {
        return input.replace("[^a-zA-Z0-9 ]".toRegex(), "")
    }

}
