package com.example.rtmplearn

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Bundle

class VideoCodec(val screenLive: ScreenLive): Thread() {
    private var mediaProjection: MediaProjection? = null
    //虚拟画布
    var virtualDisplay: VirtualDisplay? = null
    var mediaCodec: MediaCodec? = null
    var isLiving = false
    var startTime: Long = 0
    var timeStamp: Long = 0

    fun startEncode(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 400000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = mediaCodec?.createInputSurface()
        virtualDisplay = mediaProjection.createVirtualDisplay("screen-codec", 720, 1280, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null)
        isLiving = true
        start()
    }

    override fun run() {
        super.run()
        mediaCodec?.start()
        val bufferInfo = MediaCodec.BufferInfo()
        while (isLiving){
            //每两秒，添加一个关键帧
            if(System.currentTimeMillis() - timeStamp >= 2000){
                val bundle = Bundle()
                bundle.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                mediaCodec?.setParameters(bundle)
                timeStamp = System.currentTimeMillis()
            }
            //获取编码后的数据
            val index = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000)?:-1
            if(index >= 0){
                if(startTime == 0L){
                    //将dps编码时间转为毫秒
                    startTime = bufferInfo.presentationTimeUs / 1000
                }
                val outputBuffer = mediaCodec?.getOutputBuffer(index)
                val byteArray = ByteArray(bufferInfo.size)
                outputBuffer?.get(byteArray)
                FileUtils.writeContent(byteArray);
                //h264文件  ---> MP4

                //发送给rtmp
                val rtmpPackage =
                    RTMPPackage(byteArray, bufferInfo.presentationTimeUs / 1000 - startTime)
                rtmpPackage.type = RTMPPackage.RTMP_PACKAGE_TYPE_VIDEO
                screenLive.addPackage(rtmpPackage)
                mediaCodec?.releaseOutputBuffer(index, false)
            }
        }
        isLiving = false
        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
    }

    fun stopEncode(){
        isLiving = false
    }
}






















