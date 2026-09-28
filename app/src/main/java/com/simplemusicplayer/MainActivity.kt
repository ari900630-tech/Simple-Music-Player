package com.simplemusicplayer

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var player: MediaPlayer? = null
    private var currentUri: Uri? = null
    private val handler = Handler(Looper.getMainLooper())
    private var expanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playerArea = findViewById<View>(R.id.playerArea)
        val albumArt = findViewById<ImageView>(R.id.albumArt)
        val title = findViewById<TextView>(R.id.songTitle)
        val artist = findViewById<TextView>(R.id.songArtist)
        val progress = findViewById<SeekBar>(R.id.progress)
        val play = findViewById<ImageButton>(R.id.playButton)
        val rewind = findViewById<Button>(R.id.rewindButton)
        val like = findViewById<Button>(R.id.likeButton)
        val dislike = findViewById<Button>(R.id.dislikeButton)
        val stats = findViewById<Button>(R.id.statsButton)
        val bottomPlayer = findViewById<View>(R.id.bottomPlayer)

        val uri = intent.getParcelableExtra<Uri>("song_uri")
        if (uri != null) {
            currentUri = uri
            loadSong(uri, albumArt, title, artist, progress, play, like, dislike)
        }

        playerArea.setOnClickListener {
            expanded = !expanded
            stats.visibility = if (expanded) View.GONE else View.VISIBLE
            bottomPlayer.visibility = if (expanded) View.GONE else View.VISIBLE
            playerArea.alpha = if (expanded) 1f else 0.98f
            Toast.makeText(this, if (expanded) "נגן מורחב" else "נגן ממוזער", Toast.LENGTH_SHORT).show()
        }

        play.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    play.setImageResource(android.R.drawable.ic_media_play)
                    Toast.makeText(this, "מושהה", Toast.LENGTH_SHORT).show()
                } else {
                    it.start()
                    play.setImageResource(android.R.drawable.ic_media_pause)
                    Toast.makeText(this, "מנגן", Toast.LENGTH_SHORT).show()
                    updateProgress(progress, play)
                }
            } ?: Toast.makeText(this, "בחר שיר קודם", Toast.LENGTH_SHORT).show()
        }

        rewind.setOnClickListener {
            player?.let {
                it.seekTo((it.currentPosition - 10000).coerceAtLeast(0))
                Toast.makeText(this, "חזרת 10 שניות", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "בחר שיר קודם", Toast.LENGTH_SHORT).show()
        }

        progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.seekTo(value)
                    Toast.makeText(this@MainActivity, "מיקום: ${value / 1000} שניות", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onStartTrackingTouch(bar: SeekBar?) = Unit
            override fun onStopTrackingTouch(bar: SeekBar?) = Unit
        })

        like.setOnClickListener {
            currentUri?.let { song ->
                val prefs = getPreferences(MODE_PRIVATE)
                val newValue = !prefs.getBoolean("like_$song", false)
                prefs.edit().putBoolean("like_$song", newValue).putBoolean("dislike_$song", false).apply()
                like.text = if (newValue) "♥ אהבתי" else "♡ אהבתי"
                dislike.text = "♡ לא אהבתי"
                Toast.makeText(this, if (newValue) "נוסף לאהבתי" else "הוסר מאהבתי", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "בחר שיר קודם", Toast.LENGTH_SHORT).show()
        }

        dislike.setOnClickListener {
            currentUri?.let { song ->
                val prefs = getPreferences(MODE_PRIVATE)
                val newValue = !prefs.getBoolean("dislike_$song", false)
                prefs.edit().putBoolean("dislike_$song", newValue).putBoolean("like_$song", false).apply()
                dislike.text = if (newValue) "♥ לא אהבתי" else "♡ לא אהבתי"
                like.text = "♡ אהבתי"
                Toast.makeText(this, if (newValue) "נוסף ללא אהבתי" else "הוסר מלא אהבתי", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "בחר שיר קודם", Toast.LENGTH_SHORT).show()
        }

        stats.setOnClickListener {
            showStats()
        }
    }

    private fun loadSong(
        uri: Uri,
        albumArt: ImageView,
        title: TextView,
        artist: TextView,
        progress: SeekBar,
        play: ImageButton,
        like: Button,
        dislike: Button
    ) {
        player?.release()
        player = MediaPlayer.create(this, uri)
        player?.setOnCompletionListener {
            play.setImageResource(android.R.drawable.ic_media_play)
            Toast.makeText(this, "השיר הסתיים", Toast.LENGTH_SHORT).show()
        }
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

        val prefs = getPreferences(MODE_PRIVATE)
        like.text = if (prefs.getBoolean("like_$uri", false)) "♥ אהבתי" else "♡ אהבתי"
        dislike.text = if (prefs.getBoolean("dislike_$uri", false)) "♥ לא אהבתי" else "♡ לא אהבתי"
        play.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun showStats() {
        val prefs = getPreferences(MODE_PRIVATE)
        val liked = if (currentUri != null && prefs.getBoolean("like_$currentUri", false)) "כן" else "לא"
        val disliked = if (currentUri != null && prefs.getBoolean("dislike_$currentUri", false)) "כן" else "לא"
        val title = findViewById<TextView>(R.id.songTitle).text
        AlertDialog.Builder(this)
            .setTitle("הסטטיסטיקה שלי")
            .setMessage("שיר נוכחי: $title\nאהבתי: $liked\nלא אהבתי: $disliked\n\nנתוני השמעה מלאים יתווספו למונה ההשמעה.")
            .setPositiveButton("סגור", null)
            .show()
    }

    private fun updateProgress(progress: SeekBar, play: ImageButton) {
        handler.post(object : Runnable {
            override fun run() {
                val p = player
                if (p != null && p.isPlaying) {
                    progress.progress = p.currentPosition
                    handler.postDelayed(this, 300)
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
