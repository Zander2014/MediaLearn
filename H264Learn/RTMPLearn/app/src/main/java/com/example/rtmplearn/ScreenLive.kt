package com.example.rtmplearn

import android.media.projection.MediaProjection
import android.util.Log
import java.util.concurrent.LinkedBlockingQueue

class ScreenLive: Thread() {
    private lateinit var mediaProjection: MediaProjection
    private var isLiving = false
    private lateinit var queue: LinkedBlockingQueue<RTMPPackage>
    private lateinit var url: String
    private lateinit var videoCodec: VideoCodec
    private lateinit var audioCodec: AudioCodec

    fun startLive(url: String, mediaProjection: MediaProjection) {
        this.url = url
        this.mediaProjection = mediaProjection
        queue = LinkedBlockingQueue()
        start()
    }

    fun addPackage(rtmpPackage: RTMPPackage){
        if(isLiving){
            queue.add(rtmpPackage)
        }
    }

    override fun run() {
        super.run()
        isLiving = true
        if(!connect(url)){
            Log.i(TAG, "--------> 推送失败")
            return
        }
        videoCodec = VideoCodec(this)
        videoCodec.startEncode(mediaProjection)
        audioCodec = AudioCodec(this)
        audioCodec.startEncode()

        while (isLiving){
            var rtmpPackage: RTMPPackage? = null
            rtmpPackage = queue.take()
            Log.i(TAG, "取出数据")
            if(rtmpPackage?.buffer != null && rtmpPackage.buffer?.size != 0){
                sendData(rtmpPackage.buffer!!, rtmpPackage.buffer!!.size, rtmpPackage.tms, rtmpPackage.type)
            }
        }
    }

    fun stopLive(){
        isLiving = false
        this.mediaProjection.stop()
        videoCodec.stopEncode()
        audioCodec.stopEncode()
    }

    private external fun connect(url: String): Boolean
    private external fun sendData(data: ByteArray, len: Int, tms: Long, type: Int): Int

    companion object {
        const val TAG = "Zander"
    }
}