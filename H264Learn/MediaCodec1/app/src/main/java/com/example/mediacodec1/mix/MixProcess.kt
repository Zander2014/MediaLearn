package com.example.mediacodec1.mix

import android.annotation.SuppressLint
import android.media.*
import android.os.Environment
import com.example.mediacodec1.util.Pcm2WavUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class MixProcess {
    fun mixAudio(
        videoPath: String,
        audioPath: String,
        outputPath: String,
        startTime: Long,
        endTime: Long,
        videoVolume: Int,
        audioVolome: Int
    ) {
        val cacheDir = Environment.getExternalStorageDirectory()
        //视频转pcm
        val videoPcmFile = File(cacheDir, "video.pcm")
        decode2Pcm(videoPath, videoPcmFile.absolutePath, startTime, endTime, "audio")
        val videoPcmWavFile = File(cacheDir, videoPcmFile.name.toString() + ".wav")
        Pcm2WavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, 2, AudioFormat.ENCODING_PCM_16BIT)
            .Pcm2Wav(videoPcmFile.absolutePath, videoPcmWavFile.absolutePath)
        //音频转pcm
        val audioPcmFile = File(cacheDir, "audio.pcm")
        decode2Pcm(audioPath, audioPcmFile.absolutePath, startTime, endTime, "audio")
        val audioPcmWavFile = File(cacheDir, audioPcmFile.name.toString() + ".wav")
        Pcm2WavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, 2, AudioFormat.ENCODING_PCM_16BIT)
            .Pcm2Wav(audioPcmFile.absolutePath, audioPcmWavFile.absolutePath)
        //混音
        val mixPcmFile = File(cacheDir, "mix.pcm")
        mixPcm(videoPcmFile, audioPcmFile, mixPcmFile, videoVolume, audioVolome)
        val mixPcmWavFile = File(cacheDir, mixPcmFile.name.toString() + ".wav")
        Pcm2WavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, 2, AudioFormat.ENCODING_PCM_16BIT)
            .Pcm2Wav(mixPcmFile.absolutePath, mixPcmWavFile.absolutePath)
        //这里可以将pcm转成wav
        val wavFile = File(cacheDir, mixPcmFile.name.toString() + ".wav")
        Pcm2WavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, 2, AudioFormat.ENCODING_PCM_16BIT)
            .Pcm2Wav(mixPcmFile.absolutePath, wavFile.absolutePath)
        //合并视频音频
        mixVideoAndMusic(videoPath, mixPcmWavFile.absolutePath, outputPath, startTime, endTime)
    }

    //合并视频音频，使用MediaMuxer
    @SuppressLint("WrongConstant")
    private fun mixVideoAndMusic(videoPath: String, musicPath: String, outputPath: String, startTime: Long, endTime: Long) {
        //创建MediaMuxer，用于输出合成文件
        val mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        //创建MediaExtractor，用于提供文件
        val mediaExtractor = MediaExtractor()
        //提取视频，我们只需要合成视频中的视频轨，视频中的音频轨不需要合并，但是也需要用到音频轨中的format
        mediaExtractor.setDataSource(videoPath)
        val videoIndex = selectTrack(mediaExtractor, "video")
        val audioIndex = selectTrack(mediaExtractor, "audio")

        //获取视频轨format，给mediaMuxer添加新的视频轨
        val videoFormat = mediaExtractor.getTrackFormat(videoIndex)
        mediaMuxer.addTrack(videoFormat)

        //获取音频轨format，给mediaMuxer添加新的音频轨，这里只是用到原视频音频轨的format，保证合成格式正确
        val audioFormat = mediaExtractor.getTrackFormat(audioIndex)
        val audioBitRate = audioFormat.getInteger(MediaFormat.KEY_BIT_RATE)
        audioFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)//设置mime类型，其他参数不变
        val muxerAudioIndex = mediaMuxer.addTrack(audioFormat)
        //开启
        mediaMuxer.start()

        //这里的music是WAV是比较大的，应该要压缩一下再合并
        val musicExtractor = MediaExtractor()
        musicExtractor.setDataSource(musicPath)
        val musicTrack = selectTrack(musicExtractor, "audio")
        musicExtractor.selectTrack(musicTrack)//MediaExtractor选择轨道，后续输出
        val musicFormat = musicExtractor.getTrackFormat(musicTrack)

        var maxBufferSize = 100 * 1000
        if(musicFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)){
            maxBufferSize = musicFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        }
        val encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, //编码格式
            musicFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),//采样率
            musicFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT))//声道数
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioBitRate)//比特率
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)//编码等级
        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize)

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        encoder.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        var buffer = ByteBuffer.allocateDirect(maxBufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        var encodeDone = false
        while (!encodeDone){
            val inIndex = encoder.dequeueInputBuffer(10000)
            if(inIndex >= 0){
                val sampleTime = musicExtractor.sampleTime // 获取music当前时间戳
                if(sampleTime < 0){
                    //标记结束，便于后续mediaMuxer使用
                    encoder.queueInputBuffer(inIndex, 0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                }else{
                    val flags = musicExtractor.sampleFlags
                    val size = musicExtractor.readSampleData(buffer, 0)
                    val inputBuffer = encoder.getInputBuffer(inIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(buffer)
                    inputBuffer?.position(0)

                    encoder.queueInputBuffer(inIndex, 0, size, sampleTime, flags)
                    musicExtractor.advance()//前进一步，便于获取新的数据
                }
            }
            var outIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
            while (outIndex >= 0){//因为读入的maxBufferSize大小的数据，可能包含多帧，会多次输出
                if(bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                    encodeDone = true
                    break
                }
                val outputBuffer = encoder.getOutputBuffer(outIndex)
                //mediaMuxer写入音频轨
                mediaMuxer.writeSampleData(muxerAudioIndex, outputBuffer!!, bufferInfo)
                outputBuffer.clear()
                encoder.releaseOutputBuffer(outIndex, false)
                outIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
            }
        }
        //music 压缩 写入完毕，释放mediaExtractor的音频轨
        if(musicTrack >= 0){
            musicExtractor.unselectTrack(musicTrack)
        }

        //写入视频，视频不需要解码，直接添加
        mediaExtractor.selectTrack(videoIndex)
        mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        maxBufferSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        buffer = ByteBuffer.allocateDirect(maxBufferSize)
        while (true){
            val sampleTimeUs = mediaExtractor.sampleTime
            if(sampleTimeUs == -1L){
                break
            }
            if(sampleTimeUs < startTime){
                mediaExtractor.advance()
                continue
            }
            if(endTime != null && sampleTimeUs > endTime){
                break
            }
            bufferInfo.presentationTimeUs = sampleTimeUs - startTime + 600
            bufferInfo.flags = mediaExtractor.sampleFlags
            bufferInfo.size = mediaExtractor.readSampleData(buffer, 0)
            if(bufferInfo.size < 0){ //读取完毕
                break
            }
            mediaMuxer.writeSampleData(videoIndex, buffer, bufferInfo)
            mediaExtractor.advance()
        }

        musicExtractor.release()
        mediaExtractor.release()
        encoder.stop()
        encoder.release()
        mediaMuxer.release()
    }


    //将文件解析成PCM，使用MediaCodec解码
    @SuppressLint("WrongConstant")
    private fun decode2Pcm(oriPath: String, pcmPath: String, startTime: Long, endTime: Long, type: String) {
        if(endTime < startTime) return //非法时间
        //创建视频提取器
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(oriPath) //设置文件路径
        //获取音频轨道的索引
        val audioTrackIndex = selectTrack(mediaExtractor, type)
        //选择音频轨道
        mediaExtractor.selectTrack(audioTrackIndex)
        //设置范围
        mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_NEXT_SYNC)//定位到下一个I帧

        //创建解码器
        val audioFormat = mediaExtractor.getTrackFormat(audioTrackIndex)//获取当前轨道的format
        val mediaCodec = MediaCodec.createDecoderByType(audioFormat.getString(MediaFormat.KEY_MIME)?:"")//从format中获取音频编码类型
        mediaCodec.configure(audioFormat, null, null,0)
        mediaCodec.start()

        //设置最大音频buffer大小
        var maxBufferSize = 100 * 1000 //默认100k，音频数据应该够了
        if(audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)){
            maxBufferSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) //从format中获取
        }
        //创建输出文件
        val pcmFile = File(pcmPath)
        val pcmFileChannel = FileOutputStream(pcmFile).channel
        //为解码器提供放数据的容器
        val buffer = ByteBuffer.allocateDirect(maxBufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        //解码数据
        while (true){
            val inIndex = mediaCodec.dequeueInputBuffer(10000)
            if(inIndex >= 0){
                //获取到音频流当前的时间戳
                val sampleTimeUs = mediaExtractor.sampleTime
                if(sampleTimeUs == -1L){//非法时间戳，退出
                    break
                }else if(sampleTimeUs < startTime){//早于开始时间，跳过
                    mediaExtractor.advance()//丢弃当前帧，往前进一帧
                }else if(sampleTimeUs > endTime){//大于结束时间，结束
                    break
                }
                //从提取器中，提取一帧数据，并且设置bufferInfo
                bufferInfo.size = mediaExtractor.readSampleData(buffer, 0)
                bufferInfo.presentationTimeUs =  sampleTimeUs
                bufferInfo.flags = mediaExtractor.sampleFlags
                //获取字节数组
                val content = ByteArray(buffer.remaining())
                buffer.get(content)//原始数据
                val inputBuffer = mediaCodec.getInputBuffer(inIndex)
                inputBuffer?.clear()
                inputBuffer?.put(content)
                mediaCodec.queueInputBuffer(inIndex, 0, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags)
                //前进一帧
                mediaExtractor.advance()
            }
            //获取解码的数据
            val outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            if(outIndex >= 0){
                val outputBuffer = mediaCodec.getOutputBuffer(outIndex)
                pcmFileChannel.write(outputBuffer)//写入文件流
                mediaCodec.releaseOutputBuffer(outIndex, false)//释放容器
            }
        }
        pcmFileChannel.close()
        mediaExtractor.release()
        mediaCodec.stop()
        mediaCodec.release()
    }

    //将两个PCM混合
    private fun mixPcm(videoPcmFile: File, audioPcmFile: File, mixPcmFile: File, videoVolume: Int, audioVolume: Int) {
        //将声音转换成float，避免精度丢失引起的没声音
        val videoVolumeF = videoVolume / 100f *  1
        val audioVolumeF = audioVolume / 100f *  1

        //创建两个输入流
        val videoIn = FileInputStream(videoPcmFile)
        val audioIn = FileInputStream(audioPcmFile)
        var videoEnd = false
        var audioEnd = false
        val outputStream = FileOutputStream(mixPcmFile)
        //不同的流 用各自的buffer
        val bufferVideo = ByteArray(2048)
        val bufferAudio = ByteArray(2048)
        val bufferMix = ByteArray(2048)
        var videoByte: Short
        var audioByte: Short
        while (!videoEnd || !audioEnd){//如果视频或者音频没结束，就继续
            if(!videoEnd){
                videoEnd = videoIn.read(bufferVideo) == -1
            }
            if(!audioEnd){
                audioEnd = audioIn.read(bufferAudio) == -1
            }
            var mixByte = 0
            for(i in bufferAudio.indices step 2){
                //取出采样点，注意高低字节转换
                videoByte = ((bufferVideo[i].toInt() and 0xff) or ((bufferVideo[i+1].toInt() and 0xff) shl 8)).toShort()
                audioByte = ((bufferAudio[i].toInt() and 0xff) or ((bufferAudio[i+1].toInt() and 0xff) shl 8)).toShort()
                //合成采样点，分别乘以他们的声音 然后相加
                mixByte = (videoByte * videoVolumeF + audioByte * audioVolumeF).toInt()
                //处理采样点越界
                if(mixByte > Short.MAX_VALUE){
                    mixByte = Short.MAX_VALUE.toInt()
                }else if(mixByte < Short.MIN_VALUE){
                    mixByte = Short.MAX_VALUE.toInt()
                }
                //将采样点放入buffer，注意高低位转换
                bufferMix[i] = (mixByte and 0xff).toByte()
                bufferMix[i+1] = (mixByte ushr 8 and 0xff).toByte()
            }
            //写入文件
            outputStream.write(bufferMix)
        }
        videoIn.close()
        audioIn.close()
        outputStream.close()
    }

    //寻找音/视频轨道，一个视频中会有多个视频、音频轨
    //type audio/video
    fun selectTrack(extractor: MediaExtractor, type: String): Int {
        val numTracks = extractor.trackCount
        for(i in 0 until numTracks){
            val trackFormat = extractor.getTrackFormat(i)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if(mime!!.startsWith(type)){
                return i
            }
        }
        return -1
    }
}






























