## 课题：12 NDK开发之JNI编译与运行

java native interface

### 内容点：

1、JNI静态注册和动态注册

2、JNI方法调用原理

3、JNI函数签名由来

4、签名保存的正确方式

### 知识点：

1、java工程->JNI工程

当你创建一个C++工程的时候，这些都是默认生成的，这里只是列出区别。

1. 创建cmake目录及文件

   <img src="C学习.assets/image-20221018102006921.png" alt="image-20221018102006921" style="zoom:50%;" />

   系统默认创建在src/main/cpp中，CMakeLists.txt以及C文件都在这个目录。

2. app的build.gradle中配置cmake

   ```groovy
   android {
     defaultConfig {
       externalNativeBuild {
         cmake {
           /***
           1、Exceptions Support：启用对 C++ 异常处理的支持，
            新建工程选中此复选框Android Studio 会将 -fexceptions
            标志添加到模块级 build.gradle 文件的 cppFlags 中，Gradle 会将其传递到 CMake。
           2、Runtime Type Information Support：支持 RTTI，新建工程选中此复选框，Android Studio 会将 -frtti 标志添加到模块级 build.gradle 文件的 cppFlags 中，Gradle 会将其传递到 CMake。
            */
           cppFlags '' //默认不勾选，就是空值
         }
       }
     }
     //指定CMake脚本的路径
     externalNativeBuild {
       cmake {
         path file('src/main/cpp/CMakeLists.txt')//路径
         version '3.18.1'//版本
       }
     }
   }
   ```

3. CMakeList文件配置

   ```
   #指定版本
   cmake_minimum_required(VERSION 3.18.1)
   #SO库的名字
   project("jnilearn")
   #配置动态库
   add_library( # 名字Sets the name of the library.
           jnilearn
           # 类型：动态库Sets the library as a shared library.
           SHARED
           # 源文件路径Provides a relative path to your source file(s).
           native-lib.cpp)
   #配置需要链接的库
   find_library( # Sets the name of the path variable.
           log-lib
           
           # Specifies the name of the NDK library that
           # you want CMake to locate.
           log)
   #配置 自己的工程和链接库 进行链接
   target_link_libraries( # Specifies the target library.
           jnilearn
   
           # Links the target library to the log library
           # included in the NDK.
           ${log-lib})
   ```

4. native_lib.cpp

   ```c++
   #include <jni.h>
   #include <string>
   
   extern "C" JNIEXPORT jstring JNICALL
   Java_com_jnilearn_MainActivity_stringFromJNI(
           JNIEnv* env,
           jobject /* this */) {
       std::string hello = "Hello from C++";
       return env->NewStringUTF(hello.c_str());
   }
   ```

   1、extern "C"

   指定cpp文件的代码用C编译器进行编译。

   C和C++是两个不同的编译器进行编译，他们对函数最终生成的符号不同，因为C没有重载的概念，符号就是函数名，但是C++有重载，符号是函数名_参数变量类型。

   2、JNIEXPORT

   是一个宏，代表访问权限，default是可访问、hiden是不可见，外部不可调用

   ```c++
   #define JNIEXPORT  __attribute__ ((visibility ("default")))
   ```

   3、返回值jstring

   返回类型，也需要将c的类型转化成Java类型。

   ```c++
   std::string hello = "Hello from C++";
   return env->NewStringUTF(hello.c_str());
   ```

   4、JNICALL

   标识这是一个jni函数，要不要都行

   5、Java_com_jnilearn_MainActivity_stringFromJNI 

   这是一个静态注册，

   ```
   命名方式：Java_包名_类名_方法名
   ```

   6、参数JNIEnv

   属于JNI的一个环境，可以调用一系列JNI的方法，来连通Java和C++的内存。可以看作是一个转换器。

   7、参数jstring

   实际上是一个Java方法区的内存地址，我们需要将其转化成C++的内存地址后才可以使用

   ```c++
   const char* c = env->GetStringUTFChar(jstr, isCopy);//isCopy 0不复制，1复制一份到C的内存。
   
   delete c;//用完之后需要释放
   ```

   8、参数Jobject，Jclass

   如果Java方法是普通方法，传过来的就是Jobject

   如果Java方法是静态方法，传过来的就是Jclass

   9、C调用Java

   实质上是使用反射。

   ```
   //1、获取class
   jclass aClass = env->GetObjectClass(thiz);
   //2、获取属性
   jfiledID idText = env->GetFiledID(aClass, "text", "Ljava/lang/String;";
   //获取方法
   //env->GetMethodID(aClass, "callback", "(I)V");
   //3、赋值
   jstring t1 = env->NewStringUTF("hi");
   env->SetObjectField(thiz, idText, t1);
   ```

2、类型转换

Java -> JNI -> C，其中JNI的类型只是一个内存结构，可以看作一个指针，指向具体的数据。没有实际数据。

对象类型需要加分号

参数类型对照：

<img src="C学习.assets/image-20221018113218156.png" alt="image-20221018113218156" style="zoom:50%;" />

属性描述符和函数描述符

JNI属性描述符：也就是变量类型在JNI中的表示方式

<img src="C学习.assets/image-20221018112807385.png" alt="image-20221018112807385" style="zoom:50%;" />

[JNI系列(四)JAVA数据类型和JNI类型对照表](https://blog.csdn.net/u011781521/article/details/106955363)

3、动态注册：

Java在加载SO库时，就会调用JNI_OnLoad方法，此时在这里注册方法。注册的方法会在一个JNINativeMethod的数组中，类似一个方法注册表，表是有索引的，使用的时候直接查表就行。

```c++
jstring stringFromJNI(JNIEnv *jniEnv, jobject obj){
  return jniEnv->NewStringUTF("hello from C++");
}

static const JNINativeMethod nativeMethod[] = {
  {"fun",//Java中的函数名
   "()Ljava/lang/String;",//函数签名信息
   (void *)(stringFromJNI),//native的函数指针
  },
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved){
  JNIEnv *env = NULL;
  //初始化JNIEnv
  if(vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK){
    return JNI_FALSE;
  }
  //找到需要动态注册的JNI类
  jclass jniClass = env->FindClass("com/jni/MainActivity");
  if(nullptr == jniClass){
    return JNI_FALSE;
  }
  //动态注册
  env->RegisterNatives(jniClass, nativeMethod, sizeof(nativeMethod)/sizeof(nativeMethod[0]));
  //返回JNI使用的版本
  return JNI_VERSION_1_6;
}
```

相比于静态注册，动态注册不必在每次运行调用Native方法都去进行方法查找，所以相对来说动态注册的性能更高一些。

4、反编译工具

IDA、objection

5、密钥存储

存储在Java层是非常不安全的

存储在C层的so库相对安全

C层获取密钥的方法对apk签名进行验证，防止任何人都可以调用。

C层使用反射，获取签名（Java层签名可以通过PackageManager获取）。

具体代码参考当日资料;















