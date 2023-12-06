package com.example.anyclothesatanytime

import android.content.AsyncQueryHandler
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.core.content.getSystemService
import com.example.anyclothesatanytime.ml.MoveNet
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ChangeClothes : AppCompatActivity() {

    val paint : Paint = Paint()
    lateinit var imageProcessor : ImageProcessor
    lateinit var model : MoveNet
    lateinit var bitmap : Bitmap
    lateinit var imageView : ImageView
    lateinit var handler : Handler
    lateinit var handlerThread : HandlerThread
    lateinit var textureView : TextureView
    lateinit var cameraManager : CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_clothes)

        get_permissions()

        imageProcessor = ImageProcessor.Builder().add(ResizeOp(192,192,ResizeOp.ResizeMethod.BILINEAR)).build()
        model = MoveNet.newInstance(this)
        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        paint.setColor(Color.YELLOW)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            //每次更新幀所調用的函數
            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                bitmap = textureView.bitmap!!
                var tensorImage = TensorImage(DataType.UINT8)
                //使用張量圖像點加載為圖
                tensorImage.load(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
                inputFeature0.loadBuffer(tensorImage.buffer)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                //預測特徵輸出，取出浮點數組一（一維數組）
                //第一個坐標是y，然後是x，最後一個值為與其相關的置信度
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

                //將位圖改為可變位圖
                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888,true)
                //迭代此輸出特徵並圍繞檢測到的關鍵點繪製圓圈
                var canvas = Canvas(mutable)
                var h = bitmap.height
                var w = bitmap.width
                var x = 0

                while(x <= 49){
                    if (outputFeature0.get(x+2) > 0.45){
                        canvas.drawCircle(outputFeature0.get(x+1)*w,outputFeature0.get(x)*h,10f,paint)
                    }
                    x+=3
                }
                imageView.setImageBitmap(mutable)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }


    @Suppress("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0],object : CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                //捕獲請求
                var captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                var surface = Surface(textureView.surfaceTexture)
                //向捕獲請求添加目標
                captureRequest.addTarget(surface)

                p0.createCaptureSession(listOf(surface),object : CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(),null,null)
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {

                    }
                },handler)
            }

            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {

            }
        },handler)
    }


    //處理相機權限
    fun get_permissions(){
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA),101)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED)get_permissions()
    }
}

