package com.example.finalprojectaps

import PlaylistTrackResult
import Track
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectaps.databinding.ActivityMainBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private var currentPlaylistId ="37i9dQZEVXbLp5XoPON0wI"


    private lateinit var binding: ActivityMainBinding
    private lateinit var audioPlayer: AudioPlayer
    private var currentSongName: String = ""
    private lateinit var songName: String
    private lateinit var songNames: String

    private lateinit var hint: String
    private lateinit var myAdapter: MyAdapter
    private var myDataSet: Array<String> = arrayOf()

    private var hintShown: Boolean = false

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var winResponse: String
    private lateinit var loseResponse: String
    private lateinit var timesUpText: String
    private lateinit var timesUpResponse: String

    private var randomTrack: Track? = null

    private val API_KEY = "AIzaSyBueJPM7HZvI466Yav2aTvxf-TyP221-1I"

    private val sensorListener = object : SensorEventListener {
        private var lastUpdate: Long = 0
        private var last_x: Float = 0.0f
        private var last_y: Float = 0.0f
        private var last_z: Float = 0.0f
        private val SHAKE_THRESHOLD = 5

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }

        override fun onSensorChanged(event: SensorEvent) {
            val curTime = System.currentTimeMillis()
            if ((curTime - lastUpdate) > 100) {
                val diffTime = curTime - lastUpdate
                lastUpdate = curTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

                if (speed > SHAKE_THRESHOLD) {
                    pass()
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
    private val TIMER_DURATION: Long = 10000
    private var score: Int = 0

    private val spotifyService: SpotifyService by lazy {
        createSpotifyService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recycle.layoutManager = LinearLayoutManager(this)

        binding.resetButton.setOnClickListener {
            resetGame()
        }

        binding.modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.modeSwitch.text = "Mode: Oldies"
                currentPlaylistId = "5TgjzgVos90ntuffZgQfBD"
            } else {
                binding.modeSwitch.text = "Mode: Current"
                currentPlaylistId = "37i9dQZEVXbLp5XoPON0wI"
            }
        }

        fetchRandomSongFromPlaylist(currentPlaylistId)

        if (!API_KEY.isNullOrEmpty()) {
            println("API key: $API_KEY")
        } else {
            println("API key is empty or null")
        }

        // Initialize the adapter with an empty dataset
        myDataSet = arrayOf()
        myAdapter = MyAdapter(myDataSet)
        binding.recycle.adapter = myAdapter

        binding.answerQTextview.isEnabled = true
        binding.answerButton.isEnabled = true

        audioPlayer = AudioPlayer.getInstance(this)

        val animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        binding.imageView.startAnimation(animation)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        fetchRandomSongFromPlaylist(currentPlaylistId)

        binding.hintButton.setOnClickListener {
            if (!hintShown) {
                toggleHintVisibility()
                hintShown = true

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
//here
    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)

        }
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
        audioPlayer.pause()
    }

    private var currentHint: String = ""
    private fun resetGame() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
    private fun generateHint(songName: String): String {
        if (currentHint.isNotEmpty()) {
            return currentHint
        }

        val hintLength = songName.length
        val hintStringBuilder = StringBuilder()

        val randomIndices = songName.indices.shuffled().take(3)
        for (i in 0 until hintLength) {
            if (i in randomIndices) {
                hintStringBuilder.append(songName[i])
            } else {
                hintStringBuilder.append("_")
            }
        }

        currentHint = hintStringBuilder.toString()
        return currentHint
    }
    private fun toggleHintVisibility() {
        if (binding.recycle.visibility == View.VISIBLE) {
            binding.recycle.visibility = View.GONE
        } else {
            binding.recycle.visibility = View.VISIBLE

            hint = generateHint(songName)

            myAdapter.updateHint(hint)
            binding.hintButton.isEnabled = false
        }
    }
    private fun resetHintState() {
        hintShown = false
        binding.recycle.visibility = View.GONE
        binding.hintButton.isEnabled = true
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkAnswer() {
        val userAnswer = binding.answerQTextview.text.toString().trim()
        val cleanUserAnswer = removeSpecialCharacters(userAnswer)
        val cleanSongName = removeSpecialCharacters(songName)

        println("User's input: $userAnswer")
        val randomTrack =

            CoroutineScope(Dispatchers.Main).launch {
                val generativeModel = GenerativeModel(
                    modelName = "models/gemini-1.0-pro",
                    apiKey = "AIzaSyBueJPM7HZvI466Yav2aTvxf-TyP221-1I"
                )

                val winResponse = generativeModel.generateContent(prompt = "Create a very short phrase like you rock or congratulations").text

                val loseResponse = generativeModel.generateContent(prompt = "Create a very short phrase like try again or better luck next time").text

                if (cleanUserAnswer.equals(cleanSongName, ignoreCase = true)) {

                    if (winResponse != null) {
                        showToast(winResponse)
                    }
                binding.answerQTextview.text.clear()
                score += 5
                updateScoreText()
                    fetchRandomSongFromPlaylist(currentPlaylistId)
                resetHintState()

            } else {
                    if (loseResponse != null) {
                        showToast(loseResponse)
                    }
            binding.answerQTextview.text.clear()


                if (score > 0) {
                    score -= 5
                    updateScoreText()
                } else {
                    if (loseResponse != null) {
                        showToast(loseResponse)
                    }

                }
            }
        }
    }

    private fun pass() {
        // Fetch a new random song
        fetchRandomSongFromPlaylist(currentPlaylistId)
        binding.recycle.visibility = View.GONE
        binding.hintButton.isEnabled = true
        hint = generateHint(songName)
        myAdapter.updateHint(hint)
    }
    private fun fetchRandomSongFromPlaylist(playlistId: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val clientId = getString(R.string.spotify_client_id)
            val clientSecret = getString(R.string.spotify_client_secret)
            NetworkUtils.getToken(clientId, clientSecret) { accessToken ->
                accessToken?.let { token ->
                    spotifyService.getPlaylistTracks(playlistId, "Bearer $token")
                        .enqueue(object : Callback<PlaylistTrackResult> {
                            override fun onResponse(
                                call: Call<PlaylistTrackResult>,
                                response: Response<PlaylistTrackResult>
                            ) {
                                if (response.isSuccessful) {
                                    val playlistTrackResult = response.body()
                                    playlistTrackResult?.items?.let { tracks ->
                                        if (tracks.isNotEmpty()) {
                                            val randomTrack = tracks.random().track

                                            if (randomTrack.previewUrl.isNullOrEmpty()) {
                                                fetchRandomSongFromPlaylist(playlistId)
                                            }

                                            songName = randomTrack.name
                                            hint = songName
                                            hintShown = false
                                            currentHint = ""

                                            println(songName)
                                            println(randomTrack.name)
                                            println("Track Name: ${randomTrack.name}")
                                            println("Artist Name: ${randomTrack.artists.joinToString(", ") { it.name }}")
                                            println("Spotify URL: ${randomTrack.externalUrls.spotify}")
                                            println("Preview URL: ${randomTrack.previewUrl}")

                                            val trackNames = tracks.map { it.track.name }.toTypedArray()
                                            runOnUiThread {
                                                myDataSet = trackNames
                                                myAdapter.updateData(myDataSet, hint)
                                            }

                                            randomTrack.previewUrl?.let { previewUrl ->
                                                audioPlayer.playPreview(this@MainActivity, previewUrl)
                                            }
                                        } else {
                                            println("No tracks found for the playlist: $playlistId")
                                        }
                                    }
                                } else {
                                    println("Failed to fetch playlist tracks: ${response.message()}")
                                }
                            }

                            override fun onFailure(call: Call<PlaylistTrackResult>, t: Throwable) {
                                println("Network error: ${t.message}")
                            }
                        })
                } ?: println("Failed to retrieve access token")
            }
        }
    }

    private fun createSpotifyService(): SpotifyService {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyService::class.java)
    }

    private fun startTimer() {
        if (::timer.isInitialized) {
            timer.cancel()
        }

        timer = object : CountDownTimer(TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((TIMER_DURATION - millisUntilFinished) / 1000).toInt()
                binding.progressBar.progress = progress
            }

            override fun onFinish() {
                audioPlayer.pause()
                binding.imageView.clearAnimation()
                binding.answerQTextview.isEnabled = false
                binding.answerQTextview.text.clear()
                binding.answerButton.isEnabled = false
                binding.view.isVisible = true
                binding.resetButton.isVisible = true

                CoroutineScope(Dispatchers.Main).launch {
                    val generativeModel = GenerativeModel(
                        modelName = "models/gemini-1.0-pro",
                        apiKey = "AIzaSyBueJPM7HZvI466Yav2aTvxf-TyP221-1I"
                    )
                    val timesUpResponse = generativeModel.generateContent(prompt = "Create a very short phrase to replace times up. Make it ironically comical.").text

                    if (timesUpResponse != null) {
                        binding.timesUpText.text = timesUpResponse
                    } else {
                        binding.timesUpText.text = "Time's up!"
                    }
                    binding.timesUpText.isVisible = true
                }
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
