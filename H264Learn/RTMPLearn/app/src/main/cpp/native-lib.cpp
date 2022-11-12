#include <jni.h>
#include <string>
extern "C"{
#include "librtmp/rtmp.h"
}
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"Zander",__VA_ARGS__)
typedef struct {
    int8_t *sps;
    int16_t  sps_len;

    int8_t *pps;
    int16_t pps_len;

    RTMP *rtmp;
}Live;
Live *live = nullptr;

void prepareVideo(int8_t *data, int len, Live *live){
    for (int i = 0; i < len; ++i) {
        //0x00 0x00 0x00 0x01
        LOGI("connect %x", data[i]);
        if(i + 4 < len){
            if(data[i] == 0x00 && data[i+1] == 0x00 && data[i+2] == 0x00 && data[i+3] == 0x01){
                //找到pps,将sps和pps分开
                //0x00 0x00 0x00 0x01 0x67 sps 0x00 0x00 0x00 0x01 0x28 pps
                int type = data[i+4] & 0x1f;
                LOGI("connect--------------- %d", type);//用type判断比较准，因为32位是0x68，64位是0x28，不一样，取后5位，就是一定的是8
                if(data[i+4] == 0x28){
                    LOGI("connect %s", "0x68");
                    //去掉分隔符
                    live->sps_len = i - 4;//0x67 sps
                    live->sps = static_cast<int8_t *>(malloc(live->sps_len));
                    memcpy(live->sps, data+4, live->sps_len);

                    live->pps_len = len - i - 4; //0x68 pps
                    live->pps = static_cast<int8_t *>(malloc(live->pps_len));
                    memcpy(live->pps, data+i+4, live->pps_len);
                    LOGI("sps:%d pps%d", live->sps_len, live->pps_len);
                    break;
                }
            }
        }
    }
}

RTMPPacket *createVideoPackageSPSPPS(Live *live){
    //为RTMPPacket申请内存
    RTMPPacket *packet = (RTMPPacket*)malloc(sizeof(RTMPPacket));
    //数据包大小，16是固定字符
    int body_size = 16 + live->sps_len + live->pps_len;
    //为RTMPPacket的m_body申请内存
    RTMPPacket_Alloc(packet, body_size);
    //拼装m_body
    int i = 0;
    packet->m_body[i++] = 0x17;
    packet->m_body[i++] = 0x00;
    //CompositionTime
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    //AVC sequence header
    packet->m_body[i++] = 0x01;

    packet->m_body[i++] = live->sps[1]; //profile 如baseline、main、 high

    packet->m_body[i++] = live->sps[2]; //profile_compatibility 兼容性
    packet->m_body[i++] = live->sps[3]; //  level
    //固定写法
    packet->m_body[i++] = 0xFF;
    packet->m_body[i++] = 0xE1;
    //sps长度 占两个字节
    packet->m_body[i++] = (live->sps_len >>8) & 0xFF;//先存高8位
    packet->m_body[i++] = live->sps_len & 0xFF;//再存低8位
    //拷贝sps
    memcpy(&packet->m_body[i], live->sps, live->sps_len);
    i+=live->sps_len;
    packet->m_body[i++] = 0x01;
    //同样拼接pps
    packet->m_body[i++] = (live->pps_len >>8) & 0xFF;//先存高8位
    packet->m_body[i++] = live->pps_len & 0xFF;//再存低8位
    //拷贝sps
    memcpy(&packet->m_body[i], live->pps, live->pps_len);

    //拼接成功，添加配置信息
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;
    packet->m_hasAbsTimestamp = 0;//使用相对时间戳，非0就是绝对
    packet->m_nTimeStamp = 0;//sps pps给0就行，无所谓
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE; //数据包大小，给服务器匹配
    packet->m_nInfoField2 = live->rtmp->m_stream_id; //流ID
    return packet;
}

int sendPacket(RTMPPacket *rtmpPacket){
    int r = RTMP_SendPacket(live->rtmp, rtmpPacket, 1);
    RTMPPacket_Free(rtmpPacket);//释放RTMPPacket中的m_body
    free(rtmpPacket);//释放RTMPPacket
    return r;
}

RTMPPacket* createVideoPackage(int8_t *buf, int len, const long tms, Live *live){
    //跳过前面的4个分隔符 00 00 00 01
    buf += 4;
    len += 4;
    RTMPPacket *packet = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    int body_size = 9 + len;//9是固定字符长度
    RTMPPacket_Alloc(packet, body_size);
    if(buf[0] == 0x65){//I帧
        packet->m_body[0] = 0x17;
    }else{//非关键帧
        packet->m_body[0] = 0x27;
    }
    packet->m_body[1] = 0x01;
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;
    //长度4字节
    packet->m_body[5] = (len >> 24) & 0xFF;
    packet->m_body[6] = (len >> 16) & 0xFF;
    packet->m_body[7] = (len >> 8) & 0xFF;
    packet->m_body[8] = (len) & 0xFF;
    memcpy(&packet->m_body[9], buf, len);

    //拼接成功 配置
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel =  0x04;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nTimeStamp = tms;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

RTMPPacket* createAudioPackage(int8_t *buf, const int len, const int type, const long tms, Live *live){
    int body_size = len + 2;
    RTMPPacket *packet = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
    packet->m_body[0] = 0xAF;
    if(type == 1){//音频头
        packet->m_body[1] = 0x00;
    }else{//音频数据
        packet->m_body[1] = 0x01;
    }
    memcpy(&packet->m_body[2], buf, len);

    //拼接成功 配置
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel =  0x05;//和视频不同
    packet->m_hasAbsTimestamp = 0;
    packet->m_nTimeStamp = tms;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

int sendAudio(int8_t *buf, int len, int type, int tms){
    RTMPPacket *packet = createAudioPackage(buf, len, type, tms, live);
    int ret = sendPacket(packet);
    return ret;
}

int sendVideo(int8_t * buf, int len ,int tms){
    int ret;
    LOGI("connect %s", "sendVideo");
    if(buf[4] == 0x67){// sps pps
        LOGI("connect %s", "0x67");
        if(live && (!live->pps || !live->sps)){//sps pps信息不存在，添加
            LOGI("connect %s", "prepareVideo");
            prepareVideo(buf, len, live);
        }
    }else{
        if(buf[4] == 0x25){//关键帧,先发送sps pps
            RTMPPacket *packet = createVideoPackageSPSPPS(live);
            ret = sendPacket(packet);
            LOGI("connect %s", "关键帧,先发送sps pps");
        }
        //发送数据
        RTMPPacket *packet = createVideoPackage(buf, len, tms, live);
        ret = sendPacket(packet);
        LOGI("connect %s", "发送Video 数据");
    }
    return ret;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_rtmplearn_ScreenLive_connect(JNIEnv *env, jobject thiz, jstring _url) {
    const char* url = env->GetStringUTFChars(_url, 0);
    int ret;
    do {//do while(0)是为了更好的控制break，相当于goto语句，接着执行后续代码
        live = (Live *)(malloc(sizeof(Live)));
        memset(live, 0, sizeof(Live));
        live->rtmp = RTMP_Alloc();
        RTMP_Init(live->rtmp);
        live->rtmp->Link.timeout = 10;
        LOGI("connect %s", url);
        if(!(ret = RTMP_SetupURL(live->rtmp, (char*)url))) break;
        RTMP_EnableWrite(live->rtmp);
        LOGI("RTMP_Connect");
        if(!(ret = RTMP_Connect(live->rtmp, 0))) break;
        LOGI("RTMP_ConnectStream ");
        if(!(ret = RTMP_ConnectStream(live->rtmp, 0))) break;
        LOGI("Connect SUCCESS");
    } while (0);
    if(!ret && live){
        free(live);
        live = nullptr;
    }
    env->ReleaseStringUTFChars(_url, url);
    return ret;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_rtmplearn_ScreenLive_sendData(JNIEnv *env, jobject thiz, jbyteArray _data, jint len,
                                               jlong tms, jint type) {
    int ret;
    jbyte *data = env->GetByteArrayElements(_data, NULL);
    switch (type) {
        case 0:
            ret = sendVideo(data, len, tms);
            LOGI("send Video  length :%d", len);
            break;
        default:
            ret = sendAudio(data, len, type, tms);
            LOGI("send Audio  length :%d", len);
            break;
    }
    env->ReleaseByteArrayElements(_data, data, 0);

    return ret;
}