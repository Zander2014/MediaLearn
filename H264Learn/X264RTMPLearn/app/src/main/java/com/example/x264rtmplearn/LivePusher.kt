package com.example.x264rtmplearn

import android.util.Log
import com.example.x264rtmplearn.utils.FileUtils


open class LivePusher(
    val width: Int, val height: Int, val bitrate: Int,
    val fps: Int, val cameraId: Int
) {

    init {
        native_init()
    }
    private var videoChanel: IVideoChannel? = null
    fun setVideoChannel(videoChanel: IVideoChannel) {
        this.videoChanel = videoChanel
    }

    fun startLive(url: String) {
        videoChanel?.apply {
            native_start(url)
            startLive()
        }
    }
    external fun native_init()
    external fun native_start(path: String)
    external fun native_setVideoEncInfo(width: Int, height: Int, bitrate: Int, fps: Int)
    external fun native_pushVideo(data: ByteArray)
    external fun native_stop()
    external fun native_release()

    //native 层 回调，用来保存编码后的数据，方便检查
    open fun postData(data: ByteArray) {
        Log.i("david", "postData: " + data.size)
        FileUtils.writeBytes(data)
        FileUtils.writeContent(data)
    }
}