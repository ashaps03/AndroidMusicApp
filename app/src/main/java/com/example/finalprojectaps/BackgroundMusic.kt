package com.example.finalprojectaps

import android.content.Context
import android.media.MediaPlayer

object AudioPlayer {
    private var instance: AudioPlayer? = null
    private var mediaPlayer: MediaPlayer? = null

    fun getInstance(context: Context): AudioPlayer {
        if (instance == null) {
            instance = AudioPlayer
            instance?.initMediaPlayer(context)
        }
        return instance!!
    }

    private fun initMediaPlayer(context: Context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.forestwalk)
        mediaPlayer?.isLooping = true

        mediaPlayer?.setOnCompletionListener {
            // When the audio playback completes, restart the playback
            mediaPlayer?.start()
        }
    }

    fun play() {
        if (!mediaPlayer?.isPlaying!!) {
            mediaPlayer?.start()
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying!!) {
            mediaPlayer?.pause()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        instance = null
    }

    fun playPreview(context: Context, previewUrl: String) {
        try {
            if (mediaPlayer == null) {
                initMediaPlayer(context)
            } else {
                // Stop any currently playing audio before resetting
                mediaPlayer?.stop()
                mediaPlayer?.reset()
            }
            mediaPlayer?.setDataSource(previewUrl)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                it.start()
            }
            mediaPlayer?.setOnErrorListener { mp, what, extra ->
                println("MediaPlayer Error: $what, $extra")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
