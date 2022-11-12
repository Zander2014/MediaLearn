package com.example.rtmplearn

/**
 * val tms: Long = 0 //时间戳
 * val buffer: ByteArray? = null
 */
class RTMPPackage(val buffer: ByteArray, val tms: Long) {
    var type: Int = 0 //帧类型

    companion object {
        const val RTMP_PACKAGE_TYPE_VIDEO = 0   //视频数据
        const val RTMP_PACKAGE_TYPE_AUDIO_HEAD = 1 //音频头
        const val RTMP_PACKAGE_TYPE_AUDIO_DATA = 2  //音频数据
    }
}