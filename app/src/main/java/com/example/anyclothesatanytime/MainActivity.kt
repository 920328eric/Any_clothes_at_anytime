package com.example.anyclothesatanytime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val put_on_clothes = findViewById<Button>(R.id.upload_clothes_button)

        put_on_clothes.setOnClickListener {
            val intent = Intent(this, UploadClothes::class.java)
            startActivity(intent)
        }


    }
}