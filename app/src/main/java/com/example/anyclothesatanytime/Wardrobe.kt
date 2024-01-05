    package com.example.anyclothesatanytime

    import android.annotation.SuppressLint
    import android.content.Intent
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.widget.Button
    import android.widget.ImageButton
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView

    class Wardrobe : AppCompatActivity() {
        @SuppressLint("MissingInflatedId")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_wardrobe)

            val upload_clothes_button = findViewById<Button>(R.id.upload_clothes_button)
            val upload_pants_button = findViewById<Button>(R.id.upload_pants_button)
            val reload_button = findViewById<ImageButton>(R.id.reload)

            val clothesRecyclerView = findViewById<RecyclerView>(R.id.clothes_recyclerView)
            val pantsRecyclerView = findViewById<RecyclerView>(R.id.pants_recyclerView)

            // 設置衣服 RecyclerView
            val clothesLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            clothesRecyclerView.layoutManager = clothesLayoutManager
            clothesRecyclerView.setHasFixedSize(true)  // 提高效能

            // 設置褲子 RecyclerView
            val pantsLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            pantsRecyclerView.layoutManager = pantsLayoutManager
            pantsRecyclerView.setHasFixedSize(true)  // 提高效能

            val firebaseStorageManager = FirebaseStorageManager()

            upload_clothes_button.setOnClickListener {
                val intent = Intent(this, Upload::class.java)
                intent.putExtra("button_choose", "選擇衣服")
                intent.putExtra("button_upload", "上傳衣服")
                startActivity(intent)
            }

            upload_pants_button.setOnClickListener {
                val intent = Intent(this, Upload::class.java)
                intent.putExtra("button_choose", "選擇褲子")
                intent.putExtra("button_upload", "上傳褲子")
                startActivity(intent)
            }

            // 加載衣服
            firebaseStorageManager.getImageUrls("clothes") { clothesImageUrls ->
                val clothesAdapter = ImageAdapter(clothesImageUrls)
                clothesRecyclerView.adapter = clothesAdapter
            }

            // 加載褲子
            firebaseStorageManager.getImageUrls("pants") { pantsImageUrls ->
                val pantsAdapter = ImageAdapter(pantsImageUrls)
                pantsRecyclerView.adapter = pantsAdapter
            }

            reload_button.setOnClickListener {
                // 重新加載衣服
                firebaseStorageManager.getImageUrls("clothes") { clothesImageUrls ->
                    val clothesList = clothesImageUrls.toMutableList()
                    clothesList.shuffle()

                    val clothesAdapter = ImageAdapter(clothesList)
                    clothesRecyclerView.adapter = clothesAdapter
                }

                // 重新加載褲子
                firebaseStorageManager.getImageUrls("pants") { pantsImageUrls ->
                    val pantsList = pantsImageUrls.toMutableList()
                    pantsList.shuffle()

                    val pantsAdapter = ImageAdapter(pantsList)
                    pantsRecyclerView.adapter = pantsAdapter
                }
            }



        }
    }
