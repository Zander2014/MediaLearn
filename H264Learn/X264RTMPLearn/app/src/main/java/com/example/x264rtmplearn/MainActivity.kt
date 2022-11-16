package com.example.x264rtmplearn

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.x264rtmplearn.camerax.VideoChannel
import com.example.x264rtmplearn.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var livePusher: LivePusher? = null
    private val url =
        "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_591087800_54220404&key=a9d59b8830a8a6013a7433b24e8f22c1&schedule=rtmp&pflag=1"
    var videoChanel: IVideoChannel? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()

        initLivePusher()
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
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

    private fun initLivePusher(){
        livePusher = LivePusher(800, 480, 800000, 15, Camera.CameraInfo.CAMERA_FACING_BACK)
        videoChanel = VideoChannel(this, binding.textureView, livePusher!!)
        livePusher?.setVideoChannel(videoChanel!!)
    }

    companion object {
        // Used to load the 'x264rtmplearn' library on application startup.
        init {
            System.loadLibrary("x264rtmplearn")
        }
    }

    fun toggleCamera(view: View) {}
    fun startLive(view: View) {
        livePusher?.startLive(url)
    }
    fun stopLive(view: View) {}
}