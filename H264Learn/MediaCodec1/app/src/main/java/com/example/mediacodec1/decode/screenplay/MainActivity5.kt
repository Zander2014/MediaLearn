package com.example.mediacodec1.decode.screenplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.mediacodec1.R

class MainActivity5 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5)
        
        val mPreview = findViewById<SurfaceView>(R.id.mPreview)
        mPreview.holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                val surface = holder.surface
                val socketReceive = SocketReceive(surface)
                socketReceive.start()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                
            }

        })
    }
}