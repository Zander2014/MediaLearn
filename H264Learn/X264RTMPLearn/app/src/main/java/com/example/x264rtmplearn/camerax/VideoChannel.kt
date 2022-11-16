package com.example.x264rtmplearn.camerax

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import com.example.x264rtmplearn.IVideoChannel
import com.example.x264rtmplearn.LivePusher
import com.example.x264rtmplearn.utils.ImageUtil
import java.util.concurrent.locks.ReentrantLock


class VideoChannel(val lifecycleOwner: LifecycleOwner,val textureView: TextureView,val livePusher: LivePusher):
    IVideoChannel, Preview.OnPreviewOutputUpdateListener, ImageAnalysis.Analyzer {
    var isLiving = false
    var curFacing = CameraX.LensFacing.BACK
    //CameraX  适配摄像头宽高    摄像头
    var width = 1080
    var height = 1920
    private var handlerThread: HandlerThread? = null//读取并转换YUV数据比较耗时，用一个子线程

    init {
        handlerThread = HandlerThread("Analyze-thread")
        handlerThread?.start()
        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .setCallbackHandler(Handler(handlerThread?.looper!!))
            .setLensFacing(curFacing)
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)//渲染模式，运行丢帧，使用最近的一张图
            .setTargetResolution(Size(width, height))
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)
        imageAnalysis.analyzer = this

        val previewConfig = PreviewConfig.Builder()
            .setTargetResolution(Size(width, height))
            .setLensFacing(curFacing)
            .build()
        val preview = Preview(previewConfig)
        preview.onPreviewOutputUpdateListener = this
        //绑定CameraX，提供两个UseCase，一个用来预览，一个用来获取原数据
        CameraX.bindToLifecycle(lifecycleOwner, preview, imageAnalysis)
    }
    override fun startLive() {
        isLiving = true
    }

    //摄像头数据回调给TextureView，用来预览
    override fun onUpdated(output: Preview.PreviewOutput?) {
        output?.apply {
            if(textureView.surfaceTexture != surfaceTexture){
                if(textureView.isAvailable){
                    // 当切换摄像头时，会报错
                    val parent = textureView.parent as ViewGroup
                    parent.removeView(textureView)
                    parent.addView(textureView, 0)
                    parent.requestLayout()
                }
                textureView.setSurfaceTexture(surfaceTexture)
            }
        }
    }

    private val lock = ReentrantLock()
    private var y: ByteArray? = null
    private var u: ByteArray? = null
    private var v: ByteArray? = null
    // 图像帧数据，全局变量避免反复创建，降低gc频率
    private var nv21: ByteArray? = null
    var nv21_rotated: ByteArray? = null
    var nv12: ByteArray? = null
    //摄像头数据回调给CPU，是YUV数据，给Native编码
    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        if (!isLiving) {
            return
        }

        Log.i("zander", "analyze: ")
        // 开启直播并且已经成功连接服务器才获取i420数据
        //获取通道，这里的YUV通道就是三个，每个通道里分别放着对应的数据
        val planes = image!!.planes
        lock.lock()
        // 重复使用同一批byte数组，减少gc频率
        if (y == null) {
            y = ByteArray(planes[0].buffer.limit() - planes[0].buffer.position())//buffer.position() 就是0
            u = ByteArray(planes[1].buffer.limit() - planes[1].buffer.position())
            v = ByteArray(planes[2].buffer.limit() - planes[2].buffer.position())
            //初始化native层 编码，使用获取到的image的宽高才是真实的，我们设置的并不一定准
            //因为我们传递的数据，是旋转之后的，所以我们的宽高要互换一下。
            livePusher.native_setVideoEncInfo(
                image.height,
                image.width, 640_000,10
            )
        }
        //第一次放数据
        if (image.planes[0].buffer.remaining() == (y?.size ?: 0)) {
            planes[0].buffer[y]
            planes[1].buffer.get(u)
            planes[2].buffer.get(v)
            val stride = planes[0].rowStride
            val size = Size(image.width, image.height)
            val width: Int = size.height
            val height = planes[0].rowStride
            if (nv21 == null) {
                nv21 = ByteArray(height * width * 3 / 2)
                nv21_rotated = ByteArray(height * width * 3 / 2)
            }
            ImageUtil.yuvToNv21(y, u, v, nv21, height, width)//Android摄像头的yuv数据是NV12，需要转成NV21
            ImageUtil.nv21_rotate_to_90(nv21, nv21_rotated, height, width)//Android摄像头的数据需要旋转90°才是正的
            // 推送一帧画面nv21_rotated 到native去编码
            livePusher.native_pushVideo(nv21_rotated!!)
        }
        lock.unlock()
    }
}