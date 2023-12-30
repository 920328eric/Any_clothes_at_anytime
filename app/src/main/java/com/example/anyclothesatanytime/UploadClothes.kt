package com.example.anyclothesatanytime

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView




class UploadClothes : AppCompatActivity() {

    var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_clothes)

        val UploadClothesButton = findViewById<Button>(R.id.upload_clothes)
        val ChooseClothes = findViewById<Button>(R.id.choose_clothes)
        var imageView = findViewById<ImageView>(R.id.image_view)

        // 上傳衣服圖片
        UploadClothesButton.setOnClickListener {
            val firebaseStorageManager = FirebaseStorageManager()
            if (selectedImageUri != null) {
                firebaseStorageManager.uploadImage(this, selectedImageUri!!)

            }
        }

        // 選取圖片
        ChooseClothes.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

    }

    // 處理 startActivityForResult 的結果，即選擇圖片的結果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 取得圖片
            selectedImageUri = data?.data
            val bitmap = BitmapFactory.decodeStream(selectedImageUri?.let {
                contentResolver.openInputStream(it)
            })

            // 顯示圖片
            val imageView = findViewById<ImageView>(R.id.image_view)
            imageView.setImageBitmap(bitmap)
        }
    }

    fun clearImageView() {
        val imageView = findViewById<ImageView>(R.id.image_view)
        imageView.setImageDrawable(null)
    }
}