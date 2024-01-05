package com.example.anyclothesatanytime

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage

class FirebaseStorageManager {
    private val TAG = "FirebaseStorageManger"

    private val mStorageRef = FirebaseStorage.getInstance().reference
    private lateinit var mProgressDialog: ProgressDialog

    fun uploadImage(mContext: Context, imageURL: Uri, category: String) {
        mProgressDialog = ProgressDialog(mContext)
        mProgressDialog.setMessage("Please wait, image being uploading...")

        val timestamp = System.currentTimeMillis()
        val fileName = "clothes_$timestamp.png"

        // 使用 category 變數指定資料夾
        val uploadTask = mStorageRef.child("$category/$fileName").putFile(imageURL)

        uploadTask.addOnSuccessListener {
            // 上傳成功
            Log.e(TAG, "Image upload successfully")

            // 清空 selectedImageUri
            (mContext as Upload).selectedImageUri = null

            // 清空顯示的圖片
            (mContext as Upload).clearImageView()

            // 顯示不同的訊息
            val successMessage = when (category) {
                "clothes" -> "衣物上傳成功"
                "pants" -> "褲子上傳成功"
                else -> "上傳成功"
            }

            showToast(mContext, successMessage)

        }.addOnFailureListener {
            // 上傳失敗
            Log.e(TAG, "Image upload failed ${it.printStackTrace()}")
            showToast(mContext, "上傳失敗")
        }
    }

    fun getImageUrls(category: String, callback: (List<String>) -> Unit) {
        val storageRef = mStorageRef.child(category)
        val imageUrls = mutableListOf<String>()

        storageRef.listAll().addOnSuccessListener { result ->
            result.items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageUrls.add(uri.toString())
                    if (imageUrls.size == result.items.size) {
                        callback(imageUrls)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting image URLs for $category", exception)
        }
    }

    fun deleteImage(mContext: Context, category: String, imageName: String, callback: (Boolean) -> Unit) {
        val imageRef = mStorageRef.child("$category/$imageName")

        // 刪除圖片
        imageRef.delete().addOnSuccessListener {
            Log.d(TAG, "Image deleted successfully")
            showToast(mContext, "刪除成功")

            // 使用 Handler 在主線程上執行回調
            Handler(Looper.getMainLooper()).post {
                callback(true)
            }
        }.addOnFailureListener {
            Log.e(TAG, "Error deleting image", it)

            // 使用 Handler 在主線程上執行回調
            Handler(Looper.getMainLooper()).post {
                callback(false)
            }
        }
    }


    private fun showToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        toast.show()
    }

}