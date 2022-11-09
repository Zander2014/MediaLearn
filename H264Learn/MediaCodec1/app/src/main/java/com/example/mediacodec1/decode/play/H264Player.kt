package com.example.mediacodec1.decode.play

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import java.io.*


class H264Player: Runnable {
    lateinit var mediaCodec: MediaCodec
    lateinit var path: String
    fun init(surface: Surface, path: String){
        this.path = path
        mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 300, 300)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        mediaCodec.configure(mediaFormat, surface, null, 0)
    }

    fun play(){
        mediaCodec.start()
        Thread(this).start()
    }

    override fun run() {
        decodeH264()
    }

    private fun decodeH264(){
        var bytes: ByteArray? = null
        try {
//            bytes 数组里面
            bytes = getBytes(path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var startIndex = 0
        val bufferINfo = MediaCodec.BufferInfo()
        while (true){
            val nextIndex = findByFrame(bytes!!, startIndex+2, bytes.size)
            val inIndex = mediaCodec.dequeueInputBuffer(10000) //获取sdp的输入缓冲队列中可用的位置
            if(inIndex >= 0){
                val buff = mediaCodec.getInputBuffer(inIndex) //获取容器ByteBuffer
                val len = nextIndex - startIndex;
                buff?.put(bytes, startIndex, len)//丢一部分数据到容器
                mediaCodec.queueInputBuffer(inIndex, 0, len, 0, 0)
                startIndex = nextIndex;
            }
            val outIndex = mediaCodec.dequeueOutputBuffer(bufferINfo, 10000)
            if(outIndex >= 0){
//                Thread.sleep(1000)//睡眠一秒，控制显示频率
                mediaCodec.releaseOutputBuffer(outIndex, true)//释放解码后的数据，true表示推送到surface
            }
        }
    }

    private fun findByFrame(bytes: ByteArray, start: Int, totalSize: Int): Int {
        for (i in start..totalSize - 4) {
            if (bytes[i].toInt() == 0x00 && bytes[i + 1].toInt() == 0x00 && bytes[i + 2].toInt() == 0x00 && bytes[i + 3].toInt() == 0x01
                || bytes[i].toInt() == 0x00 && bytes[i + 1].toInt() == 0x00 && bytes[i + 2].toInt() == 0x01
            ) {
                return i
            }
        }
        return -1
    }

    @Throws(IOException::class)
    fun getBytes(path: String?): ByteArray? {
        val `is`: InputStream = DataInputStream(FileInputStream(File(path)))
        var len: Int
        val size = 1024
        var buf: ByteArray
        val bos = ByteArrayOutputStream()
        buf = ByteArray(size)
        while (`is`.read(buf, 0, size).also { len = it } != -1) bos.write(buf, 0, len)
        buf = bos.toByteArray()
        return buf
    }

}