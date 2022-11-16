//
// Created by zander on 2022/11/3.
//

#ifndef X264RTMPLEARN_JAVACALLHELPER_H
#define X264RTMPLEARN_JAVACALLHELPER_H

//标记线程 因为子线程需要attach
#define THREAD_MAIN 1
#define THREAD_CHILD 2

class JavaCallHelper {
public:
    JavaCallHelper(JavaVM *_javaVm, JNIEnv *_jniEnv, const jobject &_jobj);
    virtual ~JavaCallHelper();

    /**
     * 发送数据
     * @param data 数据
     * @param len 数据长度
     * @param thread 线程，默认是主线程
     */

    void postH264(char *data, int len, int thread=THREAD_MAIN);

    //定义变量保存构造方法的各参数，因为从方法调用传过来的对象，会在方法结束释放
    JavaVM *javaVM;//如果在子线程，需要用JavaVM来获取java对象
    JNIEnv *jniEnv;
    jobject jobj;
    //根据jmethodid调用java方法，分为 子线程调用 和 主线程调用
    jmethodID jmid_postData;
};


#endif //X264RTMPLEARN_JAVACALLHELPER_H
