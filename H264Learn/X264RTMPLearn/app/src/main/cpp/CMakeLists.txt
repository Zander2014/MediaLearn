# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("x264rtmplearn")
message("--------zander------------")
#CMAKE_SOURCE_DIR系统变量
message(${CMAKE_SOURCE_DIR})
message(${ANDROID_ABI})#老版本的变量，用这个的话，路径没错，但是编译器写代码时会不认识，会识别不了库文件等
message(${CMAKE_ANDROID_ARCH_ABI})
#提供头文件所在路径
include_directories(${CMAKE_SOURCE_DIR}/x264/${CMAKE_ANDROID_ARCH_ABI}/include)
message("--------zander------------")
message(${CMAKE_SOURCE_DIR}/x264/${Android_ABI}/include)
# 提供库的路径，默认会找ndk目录下的库
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/x264/${CMAKE_ANDROID_ARCH_ABI}/lib")
include_directories(${CMAKE_SOURCE_DIR}/librtmp)
add_subdirectory(librtmp)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

file(GLOB cpp_source *.cpp)
add_library( # Sets the name of the library.
        x264rtmplearn

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${cpp_source})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        x264rtmplearn

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        x264 #添加链接库，打包时候会用，会去系统默认库和上面SET库的路径去查找
        rtmp)