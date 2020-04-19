//
// Created by simpo on 2020/4/15.
// 专门用于c++回调java层的回调工具类
//

#ifndef PROMUSIC_WLCALLJAVA_H
#define PROMUSIC_WLCALLJAVA_H

#include <jni.h>
#include <linux/stddef.h>
#include "AndroidLog.h"

#define MAIN_THREAD 0
#define CHILD_THREAD 1

class WlCallJava {

public:

    _JavaVM *javaVm = NULL;
    _JNIEnv *jniEnv = NULL;
    jobject job = NULL;

    jmethodID jmid_parpared;
    jmethodID jmid_load;
    jmethodID jmid_timeinfo;
    jmethodID jmid_error;
    jmethodID jmid_complete;

public:

    WlCallJava(_JavaVM *javaVm, _JNIEnv *jniEnv, jobject *job);

    ~WlCallJava();

    void onCallPrepared(int type);

    void onCallLoad(int type, bool load);

    void onCallTimeInfo(int type, int curr, int total);

    void onCallComplete(int type);

    void onCallError(int type, int code, char *msg);

};


#endif
