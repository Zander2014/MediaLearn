package com.example.mediacodec1.encode.projection

import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import com.example.mediacodec1.util.FileUtils

class H264Encoder(): Thread() {
    lateinit var mediaProjection: MediaProjection
    var width: Int = 0
    var height: Int = 0
    lateinit var mediaCodec: MediaCodec

    constructor(projection: MediaProjection) : this() {
        this.mediaProjection = projection
        this.width = 1080
        this.height = 1920

        val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)

        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = mediaCodec.createInputSurface()
        mediaProjection.createVirtualDisplay("zander", width, height, 2,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null)
    }

    override fun run() {
        super.run()
        mediaCodec.start()
        val bufferInfo = MediaCodec.BufferInfo()
        while (true){
            //直接拿到输出，不用管输入，输入已经被实现了
            val outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            if(outIndex >= 0){
                val byteBuffer = mediaCodec.getOutputBuffer(outIndex)
                val ba = ByteArray(byteBuffer!!.remaining())
                byteBuffer.get(ba)
                FileUtils.writeBytes(ba)
                FileUtils.writeContent(ba)
                mediaCodec.releaseOutputBuffer(outIndex, false)
            }
        }
    }
}