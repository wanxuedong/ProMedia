//
// Created by mac on 2021/12/10.
//
#include "jni.h"

#ifndef TEST_AUDIOCALLJAVA_H
#define TEST_AUDIOCALLJAVA_H

#include "AndroidLog.h"


#define MAIN_THREAD 0
#define CHILD_THREAD 1

class AudioCallJava {

    _JavaVM *javaVM = NULL;
    JNIEnv *jniEnv = NULL;
    jobject jobj;

    jmethodID jmid_start;
    jmethodID jmid_load;
    jmethodID jmid_loading;
    jmethodID jmid_timeinfo;
    jmethodID jmid_error;
    jmethodID jmid_complete;
    jmethodID jmid_soundinfo;
    jmethodID jmid_volumedb;

public:
    AudioCallJava(JavaVM
                  *jvm,
                  JNIEnv *env, jobject
                  *obj);

    ~AudioCallJava();

    void onCallStart(int type);

    void onCallLoad(int type, bool load);

    void onCallLoading(int type, bool load);

    void onCallTime(int type, int curr, int total);

    void onCallError(int type, int code, char *msg);

    void onCallComplete(int type);

    void onCallVolumeDB(int type, int db);

    void onCallSoundInfo(int type, char *key, char *value);

};


#endif //TEST_AUDIOCALLJAVA_H
