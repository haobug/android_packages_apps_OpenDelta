#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include "applypatch_jni.h"
#include "Zip.h"
#include "expr.h"
#include "mtdutils/mounts.h"
#include "install.h"


#ifdef __cplusplus
extern "C" {
#endif

static jstring JNICALL NAME(sayHello)(JNIEnv *env, jobject clazz) {
    return (*env)->NewStringUTF(env, "I'm Hello from C");
}

static jint JNICALL NAME(applypatchNative)(JNIEnv *env, jclass clazz,
										jint argc, jobjectArray argv_strs){
	char **argv = (char **)malloc(argc * sizeof(char *));
	int i = 0;
	int ret = -1;

	if(argv == NULL)
		return 1;

	for(;i < argc; i+=1){
		jstring arg = (jstring)(*env)->GetObjectArrayElement(env, argv_strs, i);
		argv[i] = (*env)->GetStringUTFChars(env, arg, 0);
		LOGD("[form jni] argv[%d]:%s", i, argv[i]);
	}
	ret = applypatch_main(argc, argv);
	free(argv);
	return ret;
}

#define SCRIPT_NAME "META-INF/com/google/android/updater-script"
/*static jint JNICALL NAME(applypatchZip)(JNIEnv *env, jclass clazz,
										jstring zip_filename){
	int ret = 0;
	

	return ret;
}*/

static JNINativeMethod methods[] = {
  { "sayHello", "()Ljava/lang/String;", (void *)NAME(sayHello) },
  { "applypatchNative","(I[Ljava/lang/String;)I",(void *)NAME(applypatchNative) },
/*  { "applypatchZip","(Ljava/lang/String;)I",(void *) NAME(applypatchZip) },*/
};

static const char *classPathName = "com/hikemobile/opendelta/ApplyPatch";
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jclass clazz;
    //获取JNI环境对象
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    //注册本地方法.Load 目标类
    clazz = (*env)->FindClass(env, classPathName);
    if (clazz == NULL) {
        return JNI_ERR;
    }
    //注册本地native方法
    if((*env)->RegisterNatives(env, clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_4;
}


#ifdef __cplusplus
}
#endif
