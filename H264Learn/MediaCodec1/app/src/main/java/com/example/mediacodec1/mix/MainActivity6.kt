package com.example.mediacodec1.mix

import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.mediacodec1.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity6 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main6)

        Thread {
            val aacPath =
                File(Environment.getExternalStorageDirectory(), "music.mp3").absolutePath
            val videoPath =
                File(Environment.getExternalStorageDirectory(), "input.mp4").absolutePath
            try {
                copyAssets("music.mp3", aacPath)
                copyAssets("input.mp4", videoPath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()

        Timer().schedule(object : TimerTask(){
            override fun run() {
                val cacheDir = Environment.getExternalStorageDirectory()
                val videoFile = File(cacheDir, "input.mp4")
                val audioFile = File(cacheDir, "music.mp3")
                val outputFile = File(cacheDir, "output.mp4")
                Thread {
                    MixProcess().mixAudio(
                        videoFile.absolutePath,
                        audioFile.absolutePath,
                        outputFile.absolutePath,
                        60 * 1000 * 1000,
                        100 * 1000 * 1000,
                        50,
                        20
                    )
                }.start()
            }
        }, 5000)
    }

    @Throws(IOException::class)
    private fun copyAssets(assetsName: String, path: String) {
        val assetFileDescriptor = assets.openFd(assetsName)
        val from = FileInputStream(assetFileDescriptor.fileDescriptor).channel
        val to = FileOutputStream(path).channel
        from.transferTo(assetFileDescriptor.startOffset, assetFileDescriptor.length, to)
    }
}