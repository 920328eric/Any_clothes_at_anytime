package com.example.anyclothesatanytime

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class UploadClothes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_clothes)

        val UploadClothesButton = findViewById<Button>(R.id.upload_clothes)
        val ChooseClothes = findViewById<Button>(R.id.choose_clothes)
        val imageView = findViewById<ImageView>(R.id.image_view)

        // 上傳圖片
        UploadClothesButton.setOnClickListener {

        }

        //選取圖片
        ChooseClothes.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)

            //選擇任何類型的圖片
            intent.type = "image/*"

            //requestCode 為 1。系統會開啟選擇文件的畫面，等待使用者選擇圖片
            startActivityForResult(intent, 1)

            // 顯示圖片
            val bitmap = intent.data?.let { it1 ->
                //開啟 URI 對應的輸入串流
                contentResolver.openInputStream(it1)?.let { stream ->
                    //輸入串流解碼成 Bitmap
                    BitmapFactory.decodeStream(stream)
                }
            }
            // 該 Bitmap 給 ImageView 顯示
            imageView.setImageBitmap(bitmap)
        }

    }


    // 處理 startActivityForResult 的結果，即選擇圖片的結果
    // 本類別中的 onActivityResult 方法的覆寫，當 startActivityForResult 啟動的活動结束後，結果會返回到這個方法中
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //表示使用者成功選擇了圖片並返回
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 取得圖片
            val bitmap = BitmapFactory.decodeStream(data?.data?.let {
                contentResolver.openInputStream(
                    //it 代表的是 data?.data，也就是從 Intent 中獲取的圖片的 URI
                    it
                )
            })

            // 顯示圖片
            val imageView = findViewById<ImageView>(R.id.image_view)
            imageView.setImageBitmap(bitmap)
        }
    }


}