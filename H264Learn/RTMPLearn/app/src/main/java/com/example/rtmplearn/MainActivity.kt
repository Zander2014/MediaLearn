package com.example.rtmplearn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.rtmplearn.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val url =
        "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_591087800_54220404&key=a9d59b8830a8a6013a7433b24e8f22c1&schedule=rtmp&pflag=1"
    private var mediaProjectionManager: MediaProjectionManager? = null

    private lateinit var binding: ActivityMainBinding
    lateinit var screenLive: ScreenLive

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()
    }

    fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
                ), 1
            )
        }
        return false
    }

    companion object {
        // Used to load the 'rtmplearn' library on application startup.
        init {
            System.loadLibrary("rtmplearn")
        }
    }

    fun startLive(view: View) {
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mediaProjectionManager!!.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 100)
    }

    fun stopLive(view: View) {
        screenLive.stopLive()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
//         mediaProjection--->产生录屏数据
            val mediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode, data!!)
            //            VideoCodec videoCodec = new VideoCodec();
//            videoCodec.startLive(mediaProjection);
            screenLive = ScreenLive()
            screenLive.startLive(url, mediaProjection!!)
        }
    }
}