#include <jni.h>
#include <string>
#include "VideoChannel.h"
#include "JavaCallHelper.h"
#include "librtmp/rtmp.h"
#include "safe_queue.h"
#include "android_log.h"

//定义变量
SafeQueue<RTMPPacket *> packets;
int isStart = 0;
JavaCallHelper *javaCallHelper;
VideoChannel *videoChannel;
JavaVM *javaVm;
JNIEnv *_env;
pthread_t pid;
uint32_t start_time;

void releasePacket(RTMPPacket *packet);
void* start(void *args){
    char *url = static_cast<char *>(args);
    RTMP *rtmp = 0;
    do{
        rtmp = RTMP_Alloc();
        if(!rtmp){
            LOGE("rtmp创建失败");
            break;
        }
        RTMP_Init(rtmp);
        rtmp->Link.timeout = 5;//设置连接超时
        int ret = RTMP_SetupURL(rtmp, url);
        if(!ret){
            LOGE("rtmp设置地址失败:%s", url);
            break;
        }
        //开启输出模式
        RTMP_EnableWrite(rtmp);
        ret = RTMP_Connect(rtmp, 0);
        if(!ret){
            LOGE("rtmp连接地址失败:%s", url);
            break;
        }
        ret = RTMP_ConnectStream(rtmp, 0);
        if (!ret) {
            LOGE("rtmp连接流失败:%s", url);
            break;
        }
        packets.setWork(1);
        RTMPPacket *packet = 0;

        start_time = RTMP_GetTime();//记录开始推流的时间
        //循环从队列获取，然后发送
        while (isStart){
            packets.pop(packet);
            if(!isStart){
                break;
            }
            if(!packet){
                continue;
            }
            //给rtmp的流id
            packet->m_nInfoField2 = rtmp->m_stream_id;
            ret = RTMP_SendPacket(rtmp, packet, 1);
            releasePacket(packet);
            if(!ret){
                LOGE("发送数据失败");
                break;
            }
        }
        releasePacket(packet);
    } while (0);
    if(rtmp){
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved){
    jint ret = -1;
    if(vm->GetEnv(reinterpret_cast<void **>(&_env), JNI_VERSION_1_4) != JNI_OK){
        return ret;
    }
    javaVm = vm;
    return JNI_VERSION_1_4;
}

void callback(RTMPPacket *packet){
    if(packet){
        packet->m_nTimeStamp = RTMP_GetTime() - start_time;
        packets.push(packet);
    }
}

void releasePacket(RTMPPacket *packet){
    if(packet){
        RTMPPacket_Free(packet);
        delete packet;
        packet = nullptr;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_x264rtmplearn_LivePusher_native_1init(JNIEnv *env, jobject thiz) {
    javaCallHelper = new JavaCallHelper(javaVm, env, thiz);
    //VideoChannel *videoChannel; //隐式调用，不需要手动释放
    videoChannel = new VideoChannel();//new的对象一定要释放，
    packets = SafeQueue<RTMPPacket *>();
    videoChannel->setVideoCallback(callback);
    videoChannel->javaCallHelper = javaCallHelper;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_x264rtmplearn_LivePusher_native_1start(JNIEnv *env, jobject thiz, jstring path) {
    if(isStart){
        return;
    }
    const char *_path = env->GetStringUTFChars(path, 0);
    char *url = new char[strlen(_path) + 1];
    strcpy(url, _path);
    isStart = 1;
    //启动线程 start相当于 run方法
    pthread_create(&pid, 0, start, url);
    env->ReleaseStringUTFChars(path, _path);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_x264rtmplearn_LivePusher_native_1setVideoEncInfo(JNIEnv *env, jobject thiz,
                                                                  jint width, jint height,
                                                                  jint bitrate, jint fps) {
    LOGE("native_1setVideoEncInfo %p", videoChannel);
    if(videoChannel){
        LOGE("setVideoEncInfo");
        videoChannel->setVideoEncInfo(width, height, fps, bitrate);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_x264rtmplearn_LivePusher_native_1pushVideo(JNIEnv *env, jobject thiz,
                                                            jbyteArray _data) {
    if(!videoChannel){
        return;
    }
    jbyte *data = env->GetByteArrayElements(_data, NULL);
    videoChannel->encodeData(data);
    env->ReleaseByteArrayElements(_data, data, 0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_x264rtmplearn_LivePusher_native_1stop(JNIEnv *env, jobject thiz) {
    if(videoChannel){

    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_x264rtmplearn_LivePusher_native_1release(JNIEnv *env, jobject thiz) {
    if(!videoChannel){
        free(videoChannel);
        videoChannel = nullptr;
    }
}