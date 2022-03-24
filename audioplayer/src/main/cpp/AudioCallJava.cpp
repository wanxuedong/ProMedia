//
// Created by mac on 2021/12/10.
//

#include "AudioCallJava.h"

AudioCallJava::AudioCallJava(_JavaVM *javaVM, JNIEnv *env, jobject *obj) {

    this->javaVM = javaVM;
    this->jniEnv = env;
    this->jobj = env->NewGlobalRef(*obj);

    jclass jlz = jniEnv->GetObjectClass(jobj);
    if (!jlz) {
        if (LOG_DEBUG) {
            LOGE("get jclass wrong");
        }
        return;
    }

    jmid_start = env->GetMethodID(jlz, "onStart", "()V");
    jmid_load = env->GetMethodID(jlz, "onLoad", "(Z)V");
    jmid_loading = env->GetMethodID(jlz, "onLoading", "(Z)V");
    jmid_timeinfo = env->GetMethodID(jlz, "onTime", "(II)V");
    jmid_error = env->GetMethodID(jlz, "onError", "(ILjava/lang/String;)V");
    jmid_complete = env->GetMethodID(jlz, "onComplete", "()V");
    jmid_volumedb = env->GetMethodID(jlz, "onCallVolumeDB", "(I)V");
    jmid_soundinfo = env->GetMethodID(jlz, "onSoundInfo",
                                      "(Ljava/lang/String;Ljava/lang/String;)V");


}

void AudioCallJava::onCallStart(int type) {

    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(jobj, jmid_start);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("get child thread jnienv wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_start);
        javaVM->DetachCurrentThread();
    }

}

void AudioCallJava::onCallLoad(int type, bool load) {

    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(jobj, jmid_load);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallLoad wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_load, load);
        javaVM->DetachCurrentThread();
    }

}

void AudioCallJava::onCallLoading(int type, bool load) {

    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(jobj, jmid_loading);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallLoading wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_loading, load);
        javaVM->DetachCurrentThread();
    }
}

void AudioCallJava::onCallTime(int type, int curr, int total) {
    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(jobj, jmid_timeinfo, curr, total);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallTimeInfo wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_timeinfo, curr, total);
        javaVM->DetachCurrentThread();
    }
}

AudioCallJava::~AudioCallJava() {

}

void AudioCallJava::onCallError(int type, int code, char *msg) {
    if (type == MAIN_THREAD) {
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jobj, jmid_error, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallError wrong");
            }
            return;
        }
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jobj, jmid_error, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
        javaVM->DetachCurrentThread();
    }
}

void AudioCallJava::onCallComplete(int type) {
    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(jobj, jmid_complete);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallComplete wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_complete);
        javaVM->DetachCurrentThread();
    }
}

void AudioCallJava::onCallVolumeDB(int type, int db) {
    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(jobj, jmid_volumedb, db);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallVolumeDB wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_volumedb, db);
        javaVM->DetachCurrentThread();
    }
}

void AudioCallJava::onCallSoundInfo(int type, char *key, char *value) {
    if (type == MAIN_THREAD) {
        jstring jkey = jniEnv->NewStringUTF(key);
        jstring jvalue = jniEnv->NewStringUTF(value);
        jniEnv->CallVoidMethod(jobj, jmid_soundinfo, jkey, jvalue);
        jniEnv->DeleteLocalRef(jkey);
        jniEnv->DeleteLocalRef(jvalue);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallSoundInfo wrong");
            }
            return;
        }
        jstring jkey = jniEnv->NewStringUTF(key);
        jstring jvalue = jniEnv->NewStringUTF(value);
        jniEnv->CallVoidMethod(jobj, jmid_soundinfo, jkey, jvalue);
        jniEnv->DeleteLocalRef(jkey);
        jniEnv->DeleteLocalRef(jvalue);
        javaVM->DetachCurrentThread();
    }
}

