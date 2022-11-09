package com.example.mediacodec1.encode.screenpush

import android.Manifest
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mediacodec1.R
import com.example.mediacodec1.util.permission.PermissionsActivity
import com.example.mediacodec1.util.permission.PermissionsChecker

class MainActivity4 : AppCompatActivity() {
    lateinit var mediaProjectionManager: MediaProjectionManager
    val permission = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    )
    private val requestDataLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
            if(mediaProjection != null){
                val socketSend = SocketSend(mediaProjection)
                socketSend.start()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        if(PermissionsChecker(this).checkPermissions(*permission)){
            PermissionsActivity.startActivityForResult(this, 1, *permission)
        }else{
            mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val screenIntent = mediaProjectionManager.createScreenCaptureIntent()
            requestDataLauncher.launch(screenIntent)
        }

    }
}