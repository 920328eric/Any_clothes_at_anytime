package com.example.anyclothesatanytime

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
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.example.anyclothesatanytime.ml.MoveNet
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.max
import kotlin.math.min

class ChangeClothes : AppCompatActivity() {
    // 創建畫筆對象，用於繪製圓圈
    val paint : Paint = Paint()
    var rsframe = 0
    var rpframe = 0
    var lsframe = 0
    var lpframe = 0
    var clothingIndex = 0 // 當前使用的衣服編號
    var pantsIndex = 0 // 當前使用的褲子編號
    var limitframe = 0
    var controlfrane = 0

    private var mediaPlayer: MediaPlayer? = null

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

        val screenWidth = getScreenWidth()

        val firebaseStorageManager = FirebaseStorageManager()
        var clothingBitmapList: MutableList<Bitmap> = mutableListOf()
        

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


                // 載入所有衣服、褲子的資源
                val clothingResources = arrayOf(R.drawable.clothes1, R.drawable.clothes2, R.drawable.clothes3,R.drawable.clothes4, R.drawable.clothes5, R.drawable.clothes6, R.drawable.clothes7,R.drawable.clothes8, R.drawable.clothes9, R.drawable.clothes10)
                val pantsResources = arrayOf(R.drawable.pants1, R.drawable.pants2, R.drawable.pants3,R.drawable.pants4, R.drawable.pants5, R.drawable.pants6, R.drawable.pants7,R.drawable.pants8, R.drawable.pants9, R.drawable.pants10)

                // 載入衣服、褲子
                val clothingBitmap: Bitmap = BitmapFactory.decodeResource(resources, clothingResources[clothingIndex])
                val pantsBitmap: Bitmap = BitmapFactory.decodeResource(resources, pantsResources[pantsIndex])

        //兩肩特徵點
                var x = 5 * 3 // 起始肩膀特徵點索引
                val endShoulderIndex = 6


                var shoulderCenterX = 0f
                var shoulderCenterY = 0f

                while (x <= endShoulderIndex * 3 + 2) {
                    if (outputFeature0.get(x + 2) > 0.3) {
                        //canvas.drawCircle(outputFeature0.get(x + 1) * w, outputFeature0.get(x) * h, 10f, paint)


                        shoulderCenterX += outputFeature0.get(x + 1) * w
                        shoulderCenterY += outputFeature0.get(x) * h
                    }
                    x += 3
                }

        //兩髖特徵點
                var hipx = 11 * 3 // 起始髖特徵點索引
                val endhipIndex = 12


                var hipCenterX = 0f
                var hipCenterY = 0f

                while (hipx <= endhipIndex * 3 + 2) {
                    if (outputFeature0.get(hipx + 2) > 0.3) {
                        //canvas.drawCircle(outputFeature0.get(hipx + 1) * w, outputFeature0.get(hipx) * h, 10f, paint)


                        hipCenterX += outputFeature0.get(hipx + 1) * w
                        hipCenterY += outputFeature0.get(hipx) * h
                    }
                    hipx += 3
                }

        //判斷有沒有照到人
                if(shoulderCenterX != 0f && shoulderCenterY != 0f && hipCenterX != 0f && hipCenterY != 0f)
                {

                //載入衣服

                    // 計算兩個肩膀關節點的中心點
                    shoulderCenterX /= 2
                    shoulderCenterY /= 2

                    // 計算肩膀寬度
                    val shoulderWidth = shoulderCenterX * 2

                    // 計算縮放比例
                    val scaleRatio = shoulderWidth / getNonTransparentWidth(clothingBitmap)

                    // 計算縮小後的寬度和高度
                    val scaledWidth = (getNonTransparentWidth(clothingBitmap) * scaleRatio).toInt()
                    val scaledHeight = (getNonTransparentHeight(clothingBitmap) * scaleRatio).toInt()

                    // 進行等比例縮小
                    val scaledClothingBitmap = Bitmap.createScaledBitmap(clothingBitmap, scaledWidth, scaledHeight, true)

                    // 計算衣服圖片的中心點，使其與兩邊肩膀的中心點保持同步
                    val clothingCenterX = shoulderCenterX
                    val clothingCenterY = shoulderCenterY

                    // 計算衣服應該放置的位置
                    val clothingX = clothingCenterX - scaledWidth / 2
                    val clothingY = clothingCenterY - scaledHeight / 2 +220

                    //計算衣服底部座標
                    val clothingBottomY = clothingCenterY + scaledHeight / 2

                    // 繪製衣服
                    canvas.drawBitmap(scaledClothingBitmap, clothingX.toFloat(), clothingY.toFloat(), null)
                    // 獲取第一張衣服的圖片 URL
//                    firebaseStorageManager.getImageUrls("clothes") { clothesImageUrls ->
//                        // 將成功獲取的 URL 列表賦值給 clothingImageUrls
//                        var clothingImageUrls: List<String> = clothesImageUrls
//                        val firstImageUrl: String? = clothingImageUrls.getOrNull(0)
//
//                        if (!firstImageUrl.isNullOrEmpty()) {
//                            try {
//                                // 下載圖片並得到 Bitmap 對象
//                                val firstImagelist: Bitmap? = Picasso.get().load(firstImageUrl).get()
//
//                                // 檢查是否成功下載圖片
//                                if (firstImagelist != null) {
//                                    // 存放到 List 中
//                                    clothingBitmapList.add(firstImagelist)
//
//                                    // 在指定位置使用 Canvas 繪製位圖
//                                    canvas.drawBitmap(firstImagelist, clothingX.toFloat(), clothingY.toFloat(), null)
//                                } else {
//                                    // 下載失敗的處理邏輯
//                                    // 可以在這裡使用預設的 Bitmap 或其他處理方式
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }


                //載入褲子


                    // 計算兩個髖關節點的中心點
                    hipCenterX /= 2
                    hipCenterY /= 2

                    // 計算兩髖寬度
                    val hipWidth = hipCenterX * 2

                    // 計算縮放比例
                    val hipscaleRatio = hipWidth / getNonTransparentWidth(pantsBitmap)

                    // 計算縮小後的寬度和高度
                    val hipscaledWidth = (getNonTransparentWidth(pantsBitmap) * hipscaleRatio).toInt() -20
                    val hipscaledHeight = (getNonTransparentHeight(pantsBitmap) * hipscaleRatio).toInt() -20

                    // 進行等比例縮小或放大
                    val hipscaledpantsBitmap = Bitmap.createScaledBitmap(pantsBitmap, hipscaledWidth, hipscaledHeight, true)

                    // 計算衣服圖片的中心點，使其與兩邊髖的中心點保持同步
                    val hippantsCenterX = hipCenterX
                    val hippantsCenterY = hipCenterY

                    // 計算褲子應該放置的位置
                    val pantsX = hippantsCenterX - hipscaledWidth / 2
                    val pantsY = hippantsCenterY - hipscaledHeight / 2 + getNonTransparentHeight(hipscaledpantsBitmap) / 2 -20

                    // 繪製褲子
                    canvas.drawBitmap(hipscaledpantsBitmap, pantsX.toFloat(), pantsY.toFloat(), null)


                    limitframe += 12
                    if( ( hipCenterY - clothingBottomY ) < 140 && limitframe >= 24){
                        limitframe = 0
                        forwardSound()
                    }
                    else if( ( hipCenterY - clothingBottomY ) > 240 && limitframe >= 24){
                        limitframe = 0
                        backwordSound()
                    }

                }
                else
                {
                    controlfrane += 12
                    if(controlfrane >= 96){
                        controlfrane = 0

                        val message = "系統正在進行推斷\n請讓相機照到所有肩膀和髖部"
                        Toast.makeText(textureView.context, message, Toast.LENGTH_SHORT).show()
                    }

                }


    // 載入選衣服、褲子按鈕、加載動畫
                val shirtRightButton: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.rightbutton)
                val pantsRightButton: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.rightbutton)
                val shirtLeftButton: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.leftbutton)
                val pantsLeftButton: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.leftbutton)
                val three: Bitmap =  BitmapFactory.decodeResource(resources, R.drawable.loading1)
                val two: Bitmap =  BitmapFactory.decodeResource(resources, R.drawable.loading2)
                val one: Bitmap =  BitmapFactory.decodeResource(resources, R.drawable.loading3)

                val screenWidthFloat = screenWidth.toFloat() - 170
                val screenWidthCenter = screenWidth.toFloat() / 2

                canvas.drawBitmap(shirtRightButton, screenWidthFloat , 40.0f, null)
                canvas.drawBitmap(pantsRightButton, screenWidthFloat, 650f, null)
                canvas.drawBitmap(shirtLeftButton, 0.0f, 40.0f, null)
                canvas.drawBitmap(pantsLeftButton, 0.0f, 650.0f, null)

            // 起始右手特徵點
                var rhandx = 9 * 3
                val endrhandxIndex = 9

                //右手座標
                var rhandX = 0f
                var rhandY = 0f

                while (rhandx <= endrhandxIndex * 3 + 2) {
                    if (outputFeature0.get(rhandx + 2) > 0.3) {
                        //canvas.drawCircle(outputFeature0.get(rhandx + 1) * w, outputFeature0.get(rhandx) * h, 10f, paint)

                        rhandX = outputFeature0.get(rhandx + 1) * w
                        rhandY = outputFeature0.get(rhandx) * h

                    }
                    rhandx += 3
                }

            // 起始左手特徵點
                var lhandx = 10 * 3
                val endlhandxIndex = 10

                //左手座標
                var lhandX = 0f
                var lhandY = 0f

                while (lhandx <= endlhandxIndex * 3 + 2) {
                    if (outputFeature0.get(lhandx + 2) > 0.3) {
                        //canvas.drawCircle(outputFeature0.get(lhandx + 1) * w, outputFeature0.get(lhandx) * h, 10f, paint)

                        lhandX = outputFeature0.get(lhandx + 1) * w
                        lhandY = outputFeature0.get(lhandx) * h

                    }
                    lhandx += 3
                }

        //衣服下一件
                if (rhandY < 350 && rhandY > 0.0 && rhandX > screenWidthCenter ) {

                    rsframe += 12

                    if(rsframe >= 24)
                    {
                        canvas.drawBitmap(three, screenWidthFloat , 40.0f, null)
                    }

                    if(rsframe >= 36)
                    {
                        canvas.drawBitmap(two, screenWidthFloat , 40.0f, null)
                    }

                    if(rsframe >= 48)
                    {
                        canvas.drawBitmap(one, screenWidthFloat , 40.0f, null)
                        rsframe = 0

                        clothingIndex ++
                        if(clothingIndex == 10){
                            clothingIndex = 0
                        }

                    }
                }
                else{
                    rsframe = 0
                }

        //褲子下一件
                if (rhandY < 1000 && rhandY > 650 && rhandX > screenWidthCenter) {

                    rpframe += 12

                    if(rpframe >= 24)
                    {
                        canvas.drawBitmap(three, screenWidthFloat , 650.0f, null)
                    }

                    if(rpframe >= 36)
                    {
                        canvas.drawBitmap(two, screenWidthFloat , 650.0f, null)
                    }

                    if(rpframe >= 48)
                    {
                        canvas.drawBitmap(one, screenWidthFloat , 650.0f, null)
                        rpframe = 0

                        pantsIndex ++
                        if(pantsIndex == 10){
                            pantsIndex = 0
                        }

                    }
                }
                else{
                    rpframe = 0
                }

        //衣服上一件
                if (lhandY < 350 && lhandY > 0.0 && lhandX < screenWidthCenter && lhandX > 0) {

                    lsframe += 12

                    if(lsframe >= 24)
                    {
                        canvas.drawBitmap(three, 0.0f, 40.0f, null)
                    }

                    if(lsframe >= 36)
                    {
                        canvas.drawBitmap(two, 0.0f, 40.0f, null)
                    }

                    if(lsframe >= 48)
                    {
                        canvas.drawBitmap(one, 0.0f, 40.0f, null)
                        lsframe = 0

                        clothingIndex --
                        if (clothingIndex < 0) {
                            clothingIndex = 9
                        }

                    }
                }
                else{
                    lsframe = 0
                }
        //褲子上一件
                if (lhandY < 1000 && lhandY > 650 && lhandX < screenWidthCenter && lhandX > 0) {

                    lpframe += 12

                    if(lpframe >= 24)
                    {
                        canvas.drawBitmap(three, 0.0f, 650.0f, null)
                    }

                    if(lpframe >= 36)
                    {
                        canvas.drawBitmap(two, 0.0f, 650.0f, null)
                    }

                    if(lpframe >= 48)
                    {
                        canvas.drawBitmap(one, 0.0f, 650.0f, null)
                        lpframe = 0

                        pantsIndex --
                        if(pantsIndex < 0){
                            pantsIndex = 9
                        }
                    }
                }
                else{
                    lpframe = 0
                }


                // 設置ImageView顯示繪製後的可變位圖
                imageView.setImageBitmap(mutable)

            }


        }
    }

    // 函數來獲取非透明部分的寬度
    fun getNonTransparentWidth(bitmap: Bitmap): Int {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var minX = bitmap.width
        var maxX = 0

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                if (pixels[x + y * bitmap.width] != 0) {
                    minX = min(minX, x)
                    maxX = max(maxX, x)
                }
            }
        }

        return maxX - minX + 1
    }

    // 函數來獲取非透明部分的高度
    fun getNonTransparentHeight(bitmap: Bitmap): Int {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var minY = bitmap.height
        var maxY = 0

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                if (pixels[x + y * bitmap.width] != 0) {
                    minY = min(minY, y)
                    maxY = max(maxY, y)
                }
            }
        }

        return maxY - minY + 1
    }

    private fun getScreenWidth(): Int {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        windowManager.defaultDisplay.getMetrics(displayMetrics)

        return displayMetrics.widthPixels
    }

    private fun forwardSound() {
        // 播放提示音，這裡使用raw資源中的音頻文件
        mediaPlayer = MediaPlayer.create(this, R.raw.forward)
        mediaPlayer?.start()

        // 設置播放結束的監聽器，釋放MediaPlayer資源
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun backwordSound() {
        // 播放提示音，這裡使用raw資源中的音頻文件
        mediaPlayer = MediaPlayer.create(this, R.raw.backword)
        mediaPlayer?.start()

        // 設置播放結束的監聽器，釋放MediaPlayer資源
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = null
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