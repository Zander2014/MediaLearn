package com.example.mediacodec1.encode.screenpush

import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.util.Log
import java.nio.ByteBuffer

class H264Encoder(private val socketSend: SocketSend, private val mediaProjection: MediaProjection): Thread() {
    lateinit var mediaCodec: MediaCodec
    val width = 720
    val height = 1280
    var sps_pps_buf: ByteArray? = null

    companion object{
        const val NAL_SPS = 7
        const val NAL_I = 5
    }
    fun startEncode() {
        val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = mediaCodec.createInputSurface()
        mediaProjection.createVirtualDisplay("zander-screen", width, height, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null)

        start()
    }

    override fun run() {
        super.run()
        mediaCodec.start()
        val bufferInfo = MediaCodec.BufferInfo()
        while (true){
            val outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            if(outIndex >= 0){
                val outputBuffer = mediaCodec.getOutputBuffer(outIndex)
                dealFrame(outputBuffer, bufferInfo)
                mediaCodec.releaseOutputBuffer(outIndex, false)
            }
        }
    }

    private fun dealFrame(outputBuffer: ByteBuffer?, bufferInfo: MediaCodec.BufferInfo) {
        var offset = 4
        outputBuffer?.apply {
            if(get(2).toInt() == 0x01){
                offset = 3
            }
            val type = get(offset).toInt() and 0x1f
            when(type){
                NAL_SPS->{
                    sps_pps_buf = ByteArray(bufferInfo.size)
                    get(sps_pps_buf)
                }
                NAL_I->{
                    val outByte = ByteArray(bufferInfo.size)
                    outputBuffer.get(outByte)
                    val sendByte = ByteArray((sps_pps_buf?.size?:0) + bufferInfo.size)
                    System.arraycopy(sps_pps_buf, 0, sendByte, 0 , sps_pps_buf?.size?:0)
                    System.arraycopy(outByte, 0, sendByte, sps_pps_buf?.size?:0, outByte.size)
                    socketSend.sendData(sendByte)
                }
                else->{
                    val outByte = ByteArray(bufferInfo.size)
                    outputBuffer.get(outByte)
                    socketSend.sendData(outByte)
                    Log.v("zander", "视频数据  " + outByte.contentToString());
                }
            }
        }
    }
}






















