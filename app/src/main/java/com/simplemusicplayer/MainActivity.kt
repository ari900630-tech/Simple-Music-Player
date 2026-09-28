package com.simplemusicplayer

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var player: MediaPlayer? = null
    private var currentUri: Uri? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val albumArt = findViewById<android.widget.ImageView>(R.id.albumArt)
        val title = findViewById<TextView>(R.id.songTitle)
        val artist = findViewById<TextView>(R.id.songArtist)
        val progress = findViewById<SeekBar>(R.id.progress)
        val play = findViewById<ImageButton>(R.id.playButton)
        val rewind = findViewById<android.widget.Button>(R.id.rewindButton)
        val like = findViewById<android.widget.Button>(R.id.likeButton)

        val uri = intent.getParcelableExtra<Uri>("song_uri")
        if (uri != null) {
            currentUri = uri
            loadSong(uri, albumArt, title, artist, progress, play)
        }

        play.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    play.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    it.start()
                    play.setImageResource(android.R.drawable.ic_media_pause)
                    updateProgress(progress, play)
                }
            }
        }

        rewind.setOnClickListener {
            player?.let { it.seekTo((it.currentPosition - 10000).coerceAtLeast(0)) }
        }

        progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser) player?.seekTo(value)
            }
            override fun onStartTrackingTouch(bar: SeekBar?) = Unit
            override fun onStopTrackingTouch(bar: SeekBar?) = Unit
        })

        like.setOnClickListener {
            like.text = if (like.text == "♥") "♡" else "♥"
            currentUri?.let { getPreferences(MODE_PRIVATE).edit().putBoolean(it.toString(), like.text == "♥").apply() }
        }

        albumArt.setOnClickListener {
            it.animate().scaleX(1.03f).scaleY(1.03f).setDuration(180).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(180).start()
            }.start()
        }
    }

    private fun loadSong(
        uri: Uri,
        albumArt: android.widget.ImageView,
        title: TextView,
        artist: TextView,
        progress: SeekBar,
        play: ImageButton
    ) {
        player?.release()
        player = MediaPlayer.create(this, uri)
        player?.setOnCompletionListener { play.setImageResource(android.R.drawable.ic_media_play) }
        player?.let { progress.max = it.duration }

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(this, uri)
            title.text = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "שיר"
            artist.text = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "אמן לא ידוע"
            val art = retriever.embeddedPicture
            if (art != null) albumArt.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.size))
        } finally {
            retriever.release()
        }

        val liked = getPreferences(MODE_PRIVATE).getBoolean(uri.toString(), false)
        findViewById<android.widget.Button>(R.id.likeButton).text = if (liked) "♥" else "♡"
        play.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun updateProgress(progress: SeekBar, play: ImageButton) {
        handler.post(object : Runnable {
            override fun run() {
                val p = player
                if (p != null && p.isPlaying) {
                    progress.progress = p.currentPosition
                    handler.postDelayed(this, 300)
                } else if (p == null) {
                    handler.removeCallbacks(this)
                }
            }
        })
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
        super.onDestroy()
    }
}
