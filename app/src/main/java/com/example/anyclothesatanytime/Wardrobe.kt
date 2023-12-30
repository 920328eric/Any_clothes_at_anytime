package com.example.anyclothesatanytime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Wardrobe : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wardrobe)

        val upload_clothes_button = findViewById<Button>(R.id.upload_clothes_button)
        val upload_pants_button = findViewById<Button>(R.id.upload_pants_button)

        upload_clothes_button.setOnClickListener {
            val intent = Intent(this, UploadClothes::class.java)
            startActivity(intent)
        }

        upload_pants_button.setOnClickListener {

        }

    }
}