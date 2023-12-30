package com.example.anyclothesatanytime

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage

class FirebaseStorageManager {
    private val TAG = "FirebaseStorageManger"

    private val mStorageRef = FirebaseStorage.getInstance().reference
    private lateinit var mProgressDialog: ProgressDialog

    fun uploadImage(mContext: Context,imageURL : Uri){
        mProgressDialog = ProgressDialog(mContext)
        mProgressDialog.setMessage("Please wait, image being uploading...")
        val uploadTask = mStorageRef.child("users/ProfilePic.png").putFile(imageURL)
        uploadTask.addOnSuccessListener {
            //success
            Log.e(TAG,"Image upload successfully")

            // 清空 selectedImageUri
            (mContext as UploadClothes).selectedImageUri = null

            // 清空顯示的圖片
            (mContext as UploadClothes).clearImageView()

            showToast(mContext, "衣服上傳成功")

        }.addOnFailureListener{
            Log.e(TAG,"Image upload failed ${it.printStackTrace()}")
            showToast(mContext, "衣服上傳失敗")
        }


    }

    private fun showToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        toast.show()
    }

}