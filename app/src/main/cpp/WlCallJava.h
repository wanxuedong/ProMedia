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

    jmethodID jmid_onlog;
    jmethodID jmid_parpared;
    jmethodID jmid_load;
    jmethodID jmid_timeinfo;
    jmethodID jmid_error;
    jmethodID jmid_complete;
    jmethodID jmid_valumedb;
    jmethodID jmid_renderyuv;
    jmethodID jmid_supportvideo;
    jmethodID jmid_initmediacodec;
    jmethodID jmid_decodeavpacket;

public:

    WlCallJava(_JavaVM *javaVm, _JNIEnv *jniEnv, jobject *job);

    ~WlCallJava();

    void onLogMessage(int type, jstring message);

    void onCallPrepared(int type);

    void onCallLoad(int type, bool load);

    void onCallTimeInfo(int type, int curr, int total);

    void onCallComplete(int type);

    void onCallError(int type, int code, char *msg);

    void onCallVolumeDB(int type, int db);

    void onCallRenderYUV(int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv);

    bool onCallIsSupportVideo(const char *ffcodecname);

    void onCallInitMediacodec(const char *mime, int width, int height, int csd0_size, int csd1_size,
                              uint8_t *csd_0, uint8_t *csd_1);

    void onCallDecodeAVPacket(int datasize, uint8_t *data);
};


#endif
