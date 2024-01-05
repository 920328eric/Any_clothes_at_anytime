package com.example.anyclothesatanytime

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val put_on_clothes = findViewById<Button>(R.id.try_clothes)
        val Wardrobe_button = findViewById<Button>(R.id.Wardrobe_button)

        put_on_clothes.setOnClickListener {
            val intent = Intent(this, ChangeClothes::class.java)
            startActivity(intent)
        }

        Wardrobe_button.setOnClickListener {
            val intent = Intent(this, Wardrobe::class.java)
            startActivity(intent)
        }


    }
}