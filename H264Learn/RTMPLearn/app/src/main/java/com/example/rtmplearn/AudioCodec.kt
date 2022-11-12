package com.example.rtmplearn

import android.annotation.SuppressLint
import android.media.*

class AudioCodec(val screenLive: ScreenLive): Thread() {
    companion object{
        const val TAG = "Zander"
    }
    var mediaCodec: MediaCodec? = null
    var audioRecord: AudioRecord? = null

    var minBufferSize = 0
    var isRecording = false
    var startTime = 0L

    @SuppressLint("MissingPermission")
    fun startEncode() {
        val format =
            MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 8820)
        format.setInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        mediaCodec?.start()
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC
            ,44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize)
        start()
    }

    override fun run() {
        super.run()
        isRecording = true
        audioRecord?.startRecording()
        val bufferInfo = MediaCodec.BufferInfo()

        //先发一个音频头
        val audioDecoderSpecificInfo = byteArrayOf(0x12, 0x08)
        val rtmpPackage = RTMPPackage(audioDecoderSpecificInfo, 0)
        rtmpPackage.type = RTMPPackage.RTMP_PACKAGE_TYPE_AUDIO_HEAD
        screenLive.addPackage(rtmpPackage)

        //发送音频数据
        val buffer = ByteArray(minBufferSize)
        while (isRecording){
            val len = audioRecord?.read(buffer, 0, buffer.size)?:0
            if(len <= 0) continue

            //得到元数据，编码
            val inIndex = mediaCodec?.dequeueInputBuffer(10000)?:-1
            if(inIndex >= 0){
                val inputBuffer = mediaCodec?.getInputBuffer(inIndex)
                inputBuffer?.clear()
                inputBuffer?.put(buffer)
                mediaCodec?.queueInputBuffer(inIndex, 0, buffer.size, System.nanoTime() / 1000, 0)
            }

            var outIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000)?:-1
            while (outIndex >= 0 && isRecording){
                val outputBuffer = mediaCodec?.getOutputBuffer(outIndex)
                val outByte = ByteArray(bufferInfo.size)
                outputBuffer?.get(outByte)
                if(startTime == 0L){
                    startTime = bufferInfo.presentationTimeUs / 1000
                }
                val rtmpPackage = RTMPPackage(outByte, bufferInfo.presentationTimeUs / 1000 - startTime)
                rtmpPackage.type = RTMPPackage.RTMP_PACKAGE_TYPE_AUDIO_DATA
                screenLive.addPackage(rtmpPackage)
                mediaCodec?.releaseOutputBuffer(outIndex, false)
                outIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0)?: -1
            }
        }
        isRecording = false
        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun stopEncode() {
        isRecording = false
    }
}






















