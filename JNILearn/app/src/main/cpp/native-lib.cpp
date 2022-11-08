#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_jnilearn_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject thiz, jstring jc) {
    std::string hello = "Hello from C++";
    env->GetObjectClass(thiz);
    const char* c = env->GetStringUTFChars(jc, 0);
    delete c;
    return env->NewStringUTF(hello.c_str());
}