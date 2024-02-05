package com.example.anyclothesatanytime

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
        val linkButton = findViewById<Button>(R.id.linkButton)

        // 使用接收到的資料設置 choose 和 upload 按鈕的文本
        Choose.text = buttonChooseText
        Upload.text = buttonUploadText

        Upload.setOnClickListener {
            Log.d("UploadButton", "Upload button clicked")
            val firebaseStorageManager = FirebaseStorageManager()
            if (selectedImageUri != null) {
                Log.d("UploadButton", "Selected image URI is not null")
                if(Choose.text == "選擇衣服"){
                    Log.d("UploadButton", "Choosing clothes image")
                    firebaseStorageManager.uploadImage(this, selectedImageUri!!, "clothes")
                }

                if(Choose.text == "選擇褲子"){
                    Log.d("UploadButton", "Choosing pants image")
                    firebaseStorageManager.uploadImage(this, selectedImageUri!!, "pants")
                }
            } else {
                Log.d("UploadButton", "Selected image URI is null")
            }
        }


        // 選取圖片
        Choose.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        linkButton.setOnClickListener(View.OnClickListener {
            // 定義你想要連結的網址
            val url = "https://www.photoroom.com/zh-tw/tools/background-remover"

            // 創建一個 Intent 並設定 Action 為 ACTION_VIEW
            val intent = Intent(Intent.ACTION_VIEW)

            // 將網址轉換為 Uri 並設定給 Intent 的 data
            intent.data = Uri.parse(url)

            // 啟動 Intent，開啟瀏覽器顯示網頁
            startActivity(intent)
        })

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