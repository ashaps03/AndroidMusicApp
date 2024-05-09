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
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }



    fun playPreview(context: Context, previewUrl: String) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            mediaPlayer?.apply {
                reset()
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
                setOnErrorListener { mp, what, extra ->
                    println("MediaPlayer Error: $what, $extra")
                    reset()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




}


