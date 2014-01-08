#ifndef _Included_com_hikemobile_opendelta_ApplyPatch
#define _Included_com_hikemobile_opendelta_ApplyPatch

/* JNI naming macros */
#define CLASS   com_hikemobile_opendelta_ApplyPatch
//这里的宏一定要这样定义，才能合成出正确的函数名称
#define NAME3(CLASS3, FUNC3) Java_##CLASS3##_##FUNC3
#define NAME2(CLASS2, FUNC2) NAME3(CLASS2, FUNC2)
#define NAME(FUNC) NAME2(CLASS, FUNC)


/* LOG routines */
#include <jni.h>
#include <android/log.h>

#define  LOG_TAG    "OpenDelta"
#define DEBUG (1)

#if DEBUG
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGA(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)
#define printf(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else /* no DEBUG */
#define  LOGV(...)
#define  LOGI(...)
#define  LOGD(...)
#define  LOGW(...)
#define  LOGE(...)
#define  LOGA(...)
#endif /* no DEBUG */

#endif /* _Included_com_hikemobile_opendelta_ApplyPatch */
