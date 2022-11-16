//
// Created by zander on 2022/11/3.
//

#ifndef X264RTMPLEARN_VIDEOCHANNEL_H
#define X264RTMPLEARN_VIDEOCHANNEL_H

#include <jni.h>
#include "x264.h"
#include "JavaCallHelper.h"
#include "librtmp/rtmp.h"

class VideoChannel {
typedef void (*VideoCallback)(RTMPPacket *packet);

public:
    VideoChannel();

    virtual ~VideoChannel();
    //创建x264编码器
    void setVideoEncInfo(int width, int height, int fps, int bitrate);

    //编码
    void encodeData(int8_t *data);
    void setVideoCallback(VideoCallback callback);
    void sendSpsPps(uint8_t *sps, int sps_len, uint8_t *pps, int pps_len);
    void sendFrame(int type, int payload, uint8_t *p_payload);

    JavaCallHelper *javaCallHelper;

private:
    x264_picture_t *pic_in = 0;
    x264_t *videoCodec = 0;
    int mWidth;
    int mHeight;
    int mFps;
    int mBitrate;
    int ySize;
    int uvSize;
    VideoCallback  callback;
};


#endif //X264RTMPLEARN_VIDEOCHANNEL_H























