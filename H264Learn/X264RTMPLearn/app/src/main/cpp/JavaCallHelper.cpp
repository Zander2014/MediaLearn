//
// Created by zander on 2022/11/3.
//
#include <jni.h>
#include "JavaCallHelper.h"
#include "android_log.h"

JavaCallHelper::JavaCallHelper(JavaVM *_javaVm, JNIEnv *_jniEnv, const jobject &_jobj) {
    this->javaVM = _javaVm;
    this->jniEnv = _jniEnv;
    this->jobj = _jniEnv->NewGlobalRef(_jobj);

    //获取Java对象对应方法的methodId
    jclass jclazz = this->jniEnv->GetObjectClass(jobj);
    jmid_postData = this->jniEnv->GetMethodID(jclazz, "postData", "([B)V");
}

JavaCallHelper::~JavaCallHelper() {
    jniEnv->DeleteGlobalRef(jobj);//释放
    jniEnv = nullptr;
    javaVM = nullptr;
}

void JavaCallHelper::postH264(char *data, int len, int thread) {
    LOGE("---> postH264 env: %p  len: %d", jniEnv, len);
    jbyteArray array = jniEnv->NewByteArray(len);
    LOGE("---> postH264 array: %p", array);
    jniEnv->SetByteArrayRegion(array, 0, len, reinterpret_cast<const jbyte *>(data));

    if(thread == THREAD_CHILD){
        //子线程需要重新获取JNIEnv
        JNIEnv * env;
        if(this->javaVM->AttachCurrentThread(&env, 0) != JNI_OK){
            return;
        }
        env->CallVoidMethod(jobj, jmid_postData, array);
        javaVM->DetachCurrentThread();
    }else{
        jniEnv->CallVoidMethod(jobj, jmid_postData, array);
    }
}


