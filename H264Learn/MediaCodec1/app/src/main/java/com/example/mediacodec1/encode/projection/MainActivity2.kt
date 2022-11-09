package com.example.mediacodec1.encode.projection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mediacodec1.R

/**
 * 将录屏的画面编码成h264文件
 */
class MainActivity2 : AppCompatActivity() {
    lateinit var projectionManager: MediaProjectionManager
    lateinit var projection: MediaProjection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        checkPermission()
    }

    fun start(view: View) {
        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        //        请求用户同意录屏
        val captureIntent: Intent = projectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || requestCode != 1) return
//        下面开始   进行录屏 ---》H264
        projection = projectionManager.getMediaProjection(resultCode, data!!)
        //        mediaProjection
        val h264Encode = H264Encoder(projection)
        h264Encode.start()
    }

    fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ), 1
            )
        }
        return false
    }
}