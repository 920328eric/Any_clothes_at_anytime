package com.example.anyclothesatanytime

import android.content.AsyncQueryHandler
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    // 創建畫筆對象，用於繪製圓圈
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

        // 獲取相機權限
        get_permissions()

        // 初始化圖像處理器、MoveNet模型、相機管理器
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(192,192,ResizeOp.ResizeMethod.BILINEAR)).build()
        model = MoveNet.newInstance(this)
        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        paint.setColor(Color.RED)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                // 當SurfaceTexture可用時打開相機
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                // SurfaceTexture的尺寸發生變化時的處理
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                // SurfaceTexture被銷毀時的處理
                return false
            }

            //每次更新幀所調用的函數
            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                // 獲取TextureView上的位圖
                bitmap = textureView.bitmap!!

                // 創建TensorImage對象，將位圖轉換為張量圖像
                var tensorImage = TensorImage(DataType.UINT8)
                //使用張量圖像點加載為圖
                tensorImage.load(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // 創建TensorBuffer對象，將處理後的張量圖像載入
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
                inputFeature0.loadBuffer(tensorImage.buffer)

                // 執行模型推斷並獲取結果
                val outputs = model.process(inputFeature0)
                // 預測特徵輸出，取出浮點數組（一維數組）
                // 第一個坐標是y，然後是x，最後一個值為與其相關的置信度
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

                // 將位圖改為可變位圖
                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888,true)
                // 在可變位圖上繪製檢測到的關鍵點
                var canvas = Canvas(mutable)
                var h = bitmap.height
                var w = bitmap.width

                // 計算兩個肩膀關節點的中心點
                val shoulderCenterX = (outputFeature0.get(5 * 3 + 1) + outputFeature0.get(6 * 3 + 1)) / 2
                val shoulderCenterY = (outputFeature0.get(5 * 3) + outputFeature0.get(6 * 3)) / 2

                // 載入衣服圖片
                val clothingBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.long_sleeves1)

                // 取得衣服圖片的寬度和高度
                val clothingWidth = clothingBitmap.width
                val clothingHeight = clothingBitmap.height

                // 計算衣服圖片的中心點
                val clothingCenterX = shoulderCenterX
                val clothingCenterY = shoulderCenterY

                // 計算衣服應該放置的位置
                val clothingX = clothingCenterX - clothingWidth / 2
                val clothingY = clothingCenterY - clothingHeight / 2

                // 繪製衣服
                canvas.drawBitmap(clothingBitmap, clothingX.toFloat(), clothingY.toFloat(), null)

                // 迭代此輸出特徵並圍繞檢測到的關鍵點繪製圓圈
                var x = 5 * 3 // 起始肩膀特徵點索引
                val endShoulderIndex = 6
                while (x <= endShoulderIndex * 3 + 2) {
                    if (outputFeature0.get(x + 2) > 0.3) {
                        canvas.drawCircle(outputFeature0.get(x + 1) * w, outputFeature0.get(x) * h, 10f, paint)
                    }
                    x += 3
                }

                // 設置ImageView顯示繪製後的可變位圖
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
        cameraManager.openCamera(cameraManager.cameraIdList[1],object : CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {

                // 相機打開後的處理
                // 捕獲請求
                var captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                var surface = Surface(textureView.surfaceTexture)
                // 向捕獲請求添加目標
                captureRequest.addTarget(surface)

                // 創建相機捕獲會話
                p0.createCaptureSession(listOf(surface),object : CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        // 相機配置成功後開始捕獲
                        p0.setRepeatingRequest(captureRequest.build(),null,null)
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                        // 相機配置失敗的處理
                    }
                },handler)
            }

            override fun onDisconnected(p0: CameraDevice) {
                // 相機斷開連接的處理
            }

            override fun onError(p0: CameraDevice, p1: Int) {
                // 相機錯誤的處理
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

