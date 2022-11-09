package com.example.mediacodec1.decode.screenplay

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface

class H264Player(val surface: Surface) {
    var mediaCodec: MediaCodec
    val width = 720
    val height = 1280
    init {
        val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec.configure(mediaFormat, surface, null, 0)
        mediaCodec.start()
    }

    fun decode(data: ByteArray) {
        Log.i("zander", "解码前长度  : " + data.size)
        val inIndex = mediaCodec.dequeueInputBuffer(10000)
        if(inIndex >= 0){
            val inputBuffer = mediaCodec.getInputBuffer(inIndex)
            inputBuffer?.clear()
            inputBuffer?.put(data)
            mediaCodec.queueInputBuffer(inIndex, 0, data.size, System.currentTimeMillis(), 0)

            val bufferInfo = MediaCodec.BufferInfo()
            var outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            Log.i("zander", "解码后长度  : " + data.size)
            while (outIndex>=0){
                mediaCodec.releaseOutputBuffer(outIndex, true)
                outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
            }
        }
    }
}





















