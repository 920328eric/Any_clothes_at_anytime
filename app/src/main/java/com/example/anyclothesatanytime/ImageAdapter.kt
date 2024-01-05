package com.example.anyclothesatanytime

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ImageAdapter(private val imageUrls: List<String>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Picasso.get().load(imageUrls[position]).into(holder.imageButton)

        holder.imageButton.setOnClickListener {
            val context = holder.itemView.context
            val imageUrl = imageUrls[position]

            // 提取文件名
            val imageName = imageUrl.substringAfterLast("/")

            // 顯示確認刪除的對話框
//            showDeleteConfirmationDialog(context, imageName)
        }
    }


    override fun getItemCount(): Int {
        return imageUrls.size
    }

//    private fun showDeleteConfirmationDialog(context: Context, imageUrl: String) {
//        Log.d(TAG, "Deleting image $imageUrl")
//
//        val imageName = extractImageName(imageUrl)
//        val category = getCategoryFromImageUrl(imageUrl)
//
//        AlertDialog.Builder(context)
//            .setTitle("確認刪除")
//            .setMessage("是否確定刪除這張圖片？")
//            .setPositiveButton("是") { _, _ ->
//                // 使用 FirebaseStorageManager 刪除圖片
//                Log.d(TAG, "Category: $category ImageName: $imageName")
//
//                val firebaseStorageManager = FirebaseStorageManager()
//                firebaseStorageManager.deleteImage(context, category, imageName) { success ->
//                    if (success) {
//                        // 使用 Handler 在主線程上執行更新 RecyclerView
//                        Handler(Looper.getMainLooper()).post {
//                            notifyDataSetChanged()
//                        }
//                    } else {
//                        Log.e(TAG, "Failed to delete image")
//                    }
//                }
//            }
//            .setNegativeButton("否", null)
//            .show()
//    }

    private fun getCategoryFromImageUrl(imageUrl: String): String {

        val splits = imageUrl.split("/")

        // 判断防止索引越界
        if (splits.size > 1) {
            return splits[splits.size - 2]
        } else {
            return "clothes"
        }
    }

    private val imageNameRegex = "/([^/]+$)".toRegex()

    private fun extractImageName(imageUrl: String): String {

        val match = imageNameRegex.find(imageUrl)
        return match?.groups?.get(1)?.value ?: ""
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageButton: ImageButton = itemView.findViewById(R.id.imageButton)
    }
}


