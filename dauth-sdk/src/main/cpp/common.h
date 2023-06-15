#ifndef __COMMON_H_INCLUDED__
#define __COMMON_H_INCLUDED__

#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>

#include <android/log.h>
#define TAG "dauth-jni"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE,TAG ,__VA_ARGS__);
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__);
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__);
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__);
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__);

#define BUF_SIZ 1024

#endif // __COMMON_H_INCLUDED__
