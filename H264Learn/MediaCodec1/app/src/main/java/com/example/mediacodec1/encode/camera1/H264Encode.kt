package com.example.mediacodec1.encode.camera1

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.example.mediacodec1.util.FileUtils

class H264Encode(width: Int, height: Int) {
    lateinit var mediaCodec: MediaCodec
    var index = 0
    var width = 0
    var height = 0
    init {
        this.width = width
        this.height = height
    }

    fun startLive(){
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE, width*height)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec.start()
    }

    fun encodeFrame(input: ByteArray){
        //输入
        val inIndex = mediaCodec.dequeueInputBuffer(10000)
        if(inIndex >= 0){
            val inBuffer = mediaCodec.getInputBuffer(inIndex)
            inBuffer?.clear()
            inBuffer?.put(input)
            mediaCodec.queueInputBuffer(inIndex, 0, input.size, computePts(), 0)
            index++
        }

        //输出
        val bufferInfo = MediaCodec.BufferInfo()
        val outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
        if(outIndex >= 0){
            val outBuffer = mediaCodec.getOutputBuffer(outIndex)
            val data = ByteArray(bufferInfo.size)
            outBuffer?.get(data)
            FileUtils.writeBytes(data)
            FileUtils.writeContent(data)
            mediaCodec.releaseOutputBuffer(outIndex, false)
        }
    }

    private fun computePts(): Long{
        return 1000000L / 15 * index //1s = 1000ms = 1000000us，除以帧率，就是每帧的时长
    }
}

















