package com.example.simplemusicplayer

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        val title = TextView(this).apply {
            text = "נגן המוזיקה שלי"
            textSize = 30f
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "המוזיקה שלך, בדרך שלך"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 40)
        }
        val enter = Button(this).apply {
            text = "כניסה לנגן"
            setOnClickListener { finish() }
        }
        root.addView(title)
        root.addView(subtitle)
        root.addView(enter, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }
}
