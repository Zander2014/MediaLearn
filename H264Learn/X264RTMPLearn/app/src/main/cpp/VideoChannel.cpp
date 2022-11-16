//
// Created by zander on 2022/11/3.
//

#include <cstring>
#include "VideoChannel.h"
#include "android_log.h"

VideoChannel::VideoChannel() {}

VideoChannel::~VideoChannel() {

}

void VideoChannel::setVideoEncInfo(int width, int height, int fps, int bitrate) {
//    初始化 x264
    mWidth = width;
    mHeight = height;
    LOGE("初始化setVideoEncInfo  mWidth %d  height %d fps %d bitrate %d",mWidth,mHeight,fps,bitrate);
    ySize = width * height;
    uvSize = ySize / 4;
    mFps = fps;
    mBitrate = bitrate;
//    定义参数 隐式声明
    x264_param_t  param ;
    //    参数赋值 对于直播业务来说  ultrafast 编码速度最快 zerolatency 0延迟
    x264_param_default_preset(&param, "ultrafast", "zerolatency");
//    最大码流级别
    param.i_level_idc = 32;
//    选取显示格式
    param.i_csp = X264_CSP_I420;
    param.i_width = width;
    param.i_height = height;
    //    两个关联帧之间B帧数量，0就是不编码B帧
    param.i_bframe = 0;
    //CPU编码方式，
    //CQF 恒定质量，如果cpu忙，为了保证质量，那就时间长
    //ABR 动态平衡策略，cpu忙，质量低一点，cpu闲，质量高一点
    //CRF 保证速度，
    param.rc.i_rc_method = X264_RC_ABR;
//单位为kb  I帧  的比特
    param.rc.i_bitrate= bitrate / 1024;

    //帧率 fps/1 就是帧率
    param.i_fps_num = fps; //这个就是分子
    param.i_fps_den = 1; //这个就是分母

//编码时如果记录时间戳，那么将会非常占用空间，因为一个时间戳long类型占8个字节，成千上万帧不敢想象。
    //但是规定了帧率，其实就可以计算出每一帧的时间
    //fps就是每秒多少帧，那每帧的时间大概就是前一帧的时间 + 1000ms/fps
//    1s/帧率 =时间

//    帧率 =1s/时间

//    分母
    param.i_timebase_den=param.i_fps_num;
//    分子
    param.i_timebase_num = param.i_fps_den;

    param.b_vfr_input= 0;//变动帧率输入，配合上面的x264_param_default_preset、i_rc_method、i_fps_num等参数去计算时间
//    i帧间隔，秒开的话，I帧要多，一般2秒，也就是2*fps
    param.i_keyint_max = 2 * fps;

// sps pps 是不是重复输出 1 重复，    0 输出一次
    param.b_repeat_headers = 1;

    //多线程
    param.i_threads = 1;

    //编码等级  baseline就是直播用的
    x264_param_apply_profile(&param, "baseline");
    //返回编码器
    videoCodec = x264_encoder_open(&param);
    LOGE("--->  初始化setVideoEncInfo2 %p", videoCodec);
    //给x264提供一个容器，我们把原始数据放进去，让x264去编码，并且给他数据存放的模式。
    pic_in = new x264_picture_t;
    x264_picture_alloc(pic_in, X264_CSP_I420, width, height);
//    初始化搞定了  简单
    LOGE("初始化setVideoEncInfo3 ");
}

void VideoChannel::encodeData(int8_t *data) {
    LOGE("--->  encodeData ");
    //这里的data是java传过来的yuv数据，已经转成NV21，数据排列是  y vu
    //前面设置的容器里数据模式 X264_CSP_I420    x264_picture_alloc(pic_in, X264_CSP_I420, width, height);
    //这里我们需要自己计算，将y、u、v的数据分别保存到pic_in的img.plane中，保存到通道中
//    x264编码
    memcpy(pic_in->img.plane[0], data, ySize);//前面width*height 都是y
    LOGE("--->  encodeData 1");
    for (int i = 0; i < uvSize; ++i) {
        //交替取v和u
//u 1   v 2  //u数据
        *(pic_in->img.plane[1] + i) = *(data + ySize + i * 2 + 1);
        //v数据
        *(pic_in->img.plane[2] + i) = *(data + ySize + i * 2);
    }
    LOGE("--->  encodeData 2");
    //nal 宏块
    //编码输出的nalu数据 放在这里，， H264  一帧对应多个nalu，放在它的p_payload中。     硬件编码中，由音源编码器来做这件事
    x264_nal_t *pp_nals;// NAL数据包
    //编码出了几个 nalu （暂时理解为帧）  1   pi_nal  1  永远是1
    int pi_nal;

    x264_picture_t pic_out;//编码后输出帧
    //编码出的数据 H264
    LOGE("--->  encodeData 3 %p", videoCodec);
    x264_encoder_encode(videoCodec, &pp_nals, &pi_nal, pic_in, &pic_out);
//借助java  --》文件
//    if (pi_nal > 0) {
//
//        for (int i = 0; i < pi_nal; ++i) {
//            LOGE("i  %d",i);
//            javaCallHelper->postH264(reinterpret_cast<char *>(pp_nals[i].p_payload), pp_nals[i].i_payload);
//        }
//    }

    LOGE("--->  encodeData 4");
    //判断是ssp pps 还是 关键帧，非关键帧，对应的去包装对应帧数据，并且发送
    uint8_t sps[100];
    uint8_t pps[100];
    int sps_len, pps_len;
    //正常来说pp_nals只有一条数据pp_nals[0]，这里用循环，可能是因为一帧数据会解析出多个nals序列
    //pp_nals[i].p_payload 里面就是这帧的数据
    for (int i = 0; i < pi_nal; ++i) {
        if (pp_nals[i].i_type == NAL_SPS) {
            // 去掉 00 00 00 01
            sps_len = pp_nals[i].i_payload - 4;
            memcpy(sps, pp_nals[i].p_payload + 4, sps_len);
//            要1  不要2      要 1  不要 2
//按照  硬解码  sps ps   数组 I帧  不要
        } else if (pp_nals[i].i_type == NAL_PPS) {
            pps_len = pp_nals[i].i_payload - 4;
            memcpy(pps, pp_nals[i].p_payload + 4, pps_len);
            //拿到pps 就表示 sps已经拿到了
            sendSpsPps(sps, sps_len, pps, pps_len);

        } else {
            //关键帧、非关键帧
            sendFrame(pp_nals[i].i_type,pp_nals[i].i_payload,pp_nals[i].p_payload);
        }

    }
    LOGE("--->  encodeData 5");
}

void VideoChannel::setVideoCallback(VideoChannel::VideoCallback callback) {
    this->callback = callback;
}

void VideoChannel::sendSpsPps(uint8_t *sps, int sps_len, uint8_t *pps, int pps_len) {
    RTMPPacket *packet = new RTMPPacket;
    int bodysize = 13 + sps_len + 3 + pps_len;
    RTMPPacket_Alloc(packet, bodysize);
    int i = 0;
    //固定头
    packet->m_body[i++] = 0x17;
    //类型
    packet->m_body[i++] = 0x00;
    //composition time 0x000000
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;

    //版本
    packet->m_body[i++] = 0x01;
    //编码规格
    packet->m_body[i++] = sps[1];
    packet->m_body[i++] = sps[2];
    packet->m_body[i++] = sps[3];
    packet->m_body[i++] = 0xFF;

    //整个sps
    packet->m_body[i++] = 0xE1;
    //sps长度
    packet->m_body[i++] = (sps_len >> 8) & 0xff;
    packet->m_body[i++] = sps_len & 0xff;
    memcpy(&packet->m_body[i], sps, sps_len);
    i += sps_len;

    //pps
    packet->m_body[i++] = 0x01;
    packet->m_body[i++] = (pps_len >> 8) & 0xff;
    packet->m_body[i++] = (pps_len) & 0xff;
    memcpy(&packet->m_body[i], pps, pps_len);


    //视频
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = bodysize;
    //随意分配一个管道（尽量避开rtmp.c中使用的）
    packet->m_nChannel = 10;
    //sps pps没有时间戳
    packet->m_nTimeStamp = 0;
    //不使用绝对时间
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    LOGE("  callback %p ",callback);
    callback(packet);//回调，添加到队列，等待rtmp轮训队列去发送
}

void VideoChannel::sendFrame(int type, int payload, uint8_t *p_payload) {
//去掉 00 00 00 01 / 00 00 01
    if (p_payload[2] == 0x00){
        payload -= 4;
        p_payload += 4;
    } else if(p_payload[2] == 0x01){
        payload -= 3;
        p_payload += 3;
    }
    RTMPPacket *packet = new RTMPPacket;
    int bodysize = 9 + payload;
    RTMPPacket_Alloc(packet, bodysize);
    RTMPPacket_Reset(packet);
//    int type = payload[0] & 0x1f;
    packet->m_body[0] = 0x27;
    //关键帧
    if (type == NAL_SLICE_IDR) {
        LOGE("关键帧");
        packet->m_body[0] = 0x17;
    }
    //类型
    packet->m_body[1] = 0x01;
    //时间戳
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;
    //数据长度 int 4个字节 相当于把int转成4个字节的byte数组
    packet->m_body[5] = (payload >> 24) & 0xff;
    packet->m_body[6] = (payload >> 16) & 0xff;
    packet->m_body[7] = (payload >> 8) & 0xff;
    packet->m_body[8] = (payload) & 0xff;

    //图片数据
    memcpy(&packet->m_body[9],p_payload,  payload);

    packet->m_hasAbsTimestamp = 0;
    packet->m_nBodySize = bodysize;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nChannel = 0x10;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    callback(packet);
}
