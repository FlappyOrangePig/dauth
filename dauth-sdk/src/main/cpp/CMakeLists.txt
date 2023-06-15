cmake_minimum_required(VERSION 3.26.4)

project("dauth-sdk")

set(CMAKE_CXX_STANDARD 14)

# 添加头文件目录
# CMAKE_SOURCE_DIR是指cpp目录
include_directories(${CMAKE_SOURCE_DIR}/../jniLibs/include/)

# 源代码编译到dauth
add_library(
        dauth
        SHARED
        dauth-jni.cpp)

# 查找内置库
find_library(log-lib log)

target_link_libraries(
        dauth
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libMPCSignModule.so
        ${log-lib}
        ${ANDROID_NDK}/sources/cxx-stl/llvm-libc++/libs/${ANDROID_ABI}/libc++_shared.so)



