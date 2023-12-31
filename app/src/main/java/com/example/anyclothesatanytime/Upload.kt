package com.example.anyclothesatanytime

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView




class Upload : AppCompatActivity() {

    var selectedImageUri: Uri? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_clothes)

        // 接收從Wardrobe活動傳遞的資料
        val buttonChooseText = intent.getStringExtra("button_choose")
        val buttonUploadText = intent.getStringExtra("button_upload")


        val Upload= findViewById<Button>(R.id.upload)
        val Choose = findViewById<Button>(R.id.choose)
        var imageView = findViewById<ImageView>(R.id.image_view)

        // 使用接收到的資料設置 choose 和 upload 按鈕的文本
        Choose.text = buttonChooseText
        Upload.text = buttonUploadText

        // 上傳衣服圖片
        Upload.setOnClickListener {
            val firebaseStorageManager = FirebaseStorageManager()
            if (selectedImageUri != null) {
                if(Choose.text == "選擇衣服圖片"){
                    firebaseStorageManager.uploadImage(this, selectedImageUri!!, "clothes")
                }

                if(Choose.text == "選擇褲子圖片"){
                    firebaseStorageManager.uploadImage(this, selectedImageUri!!, "pants")
                }
            }
        }

        // 選取圖片
        Choose.setOnClickListener {
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