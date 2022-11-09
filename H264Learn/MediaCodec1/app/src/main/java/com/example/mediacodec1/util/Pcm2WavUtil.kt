package com.example.mediacodec1.util

import android.media.AudioFormat
import android.media.AudioRecord
import java.io.FileInputStream
import java.io.FileOutputStream

//Pcm封装成WAV
class Pcm2WavUtil {
    var mBufferSize: Int = 0//音频缓存大小
    var mSampleRate = 8000 //采样率 一般设置 8000 | 16000
    var mChannelConfig = AudioFormat.CHANNEL_IN_STEREO //双声道
    var mChannelCount = 2
    var mEncoding = AudioFormat.ENCODING_PCM_16BIT

    constructor() {
        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mEncoding)
    }

    constructor(
        mSampleRate: Int,
        mChannelConfig: Int,
        mChannelCount: Int,
        mEncoding: Int
    ) {
        this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mEncoding)
        this.mSampleRate = mSampleRate
        this.mChannelConfig = mChannelConfig
        this.mChannelCount = mChannelCount
        this.mEncoding = mEncoding
    }

    fun Pcm2Wav(pcmPath: String, wavPath: String){
        val input: FileInputStream = FileInputStream(pcmPath)
        val output: FileOutputStream = FileOutputStream(wavPath)
        val totalAudioLen = input.channel.size()
        val totalDataLen = totalAudioLen + 36
        val byteRate = 16 * mSampleRate * mChannelCount / 8
        val data: ByteArray = ByteArray(totalDataLen.toInt())
        //先添加头信息
        writeWAVFileHeader(output, totalAudioLen, totalDataLen, mSampleRate.toLong(), mChannelCount, byteRate.toLong())
        //再写入pcm数据
        while (input.read(data) != -1){
            output.write(data)
        }
        input.close()
        output.close()
    }

    //可以参考WAV的文档，根据头部信息的介绍，一个字节一个字节的输入
    private fun writeWAVFileHeader(outputStream: FileOutputStream,
    totalAudioLen: Long, totalDataLen: Long, longSampleRate: Long, channelCount: Int, byteRate: Long){
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        //将 Long类型的totalDataLen，存入byte，低位到高位的存
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channelCount.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte() //data
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        outputStream.write(header, 0, 44)
    }
}




















