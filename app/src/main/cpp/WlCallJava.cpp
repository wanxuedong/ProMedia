#include "WlCallJava.h"

WlCallJava::WlCallJava(_JavaVM *javaVm, _JNIEnv *jniEnv, jobject *obj) {

    this->javaVm = javaVm;
    this->jniEnv = jniEnv;
    this->job = *obj;
    this->job = jniEnv->NewGlobalRef(job);

    jclass jcls = jniEnv->GetObjectClass(job);
    if (!jcls) {
        if (LOG_DEBUG) {
            LOGE("get jclass wrong");
        }
    }
    jmid_parpared = jniEnv->GetMethodID(jcls, "onCallPrepared", "()V");
    jmid_load = jniEnv->GetMethodID(jcls, "onCallLoad", "(Z)V");
    jmid_timeinfo = jniEnv->GetMethodID(jcls, "onCallTimeInfo", "(II)V");
    jmid_complete = jniEnv->GetMethodID(jcls, "onCallComplete", "()V");
    jmid_error = jniEnv->GetMethodID(jcls, "onCallError", "(ILjava/lang/String;)V");
    jmid_valumedb = jniEnv->GetMethodID(jcls, "onCallValumeDB", "(I)V");

}

WlCallJava::~WlCallJava() {


}

void WlCallJava::onCallPrepared(int type) {

    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(job, jmid_parpared);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv1;
        if (javaVm->AttachCurrentThread(&jniEnv1, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("get child thread jnienv worng");
            }
            return;
        }
        jniEnv1->CallVoidMethod(job, jmid_parpared);
        javaVm->DetachCurrentThread();
    }

}


void WlCallJava::onCallLoad(int type, bool load) {

    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(job, jmid_load, load);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCllLoad worng");
            }
            return;
        }
        jniEnv->CallVoidMethod(job, jmid_load, load);
        javaVm->DetachCurrentThread();
    }
}

void WlCallJava::onCallTimeInfo(int type, int curr, int total) {
    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(job, jmid_timeinfo, curr, total);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallTimeInfo worng");
            }
            return;
        }
        jniEnv->CallVoidMethod(job, jmid_timeinfo, curr, total);
        javaVm->DetachCurrentThread();
    }
}

void WlCallJava::onCallComplete(int type) {
    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(job, jmid_complete);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallComplete worng");
            }
            return;
        }
        jniEnv->CallVoidMethod(job, jmid_complete);
        javaVm->DetachCurrentThread();
    }
}

void WlCallJava::onCallError(int type, int code, char *msg) {
    if (type == MAIN_THREAD) {
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(job, jmid_error, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallTimeInfo worng");
            }
            return;
        }
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(job, jmid_error, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
        javaVm->DetachCurrentThread();
    }
}

void WlCallJava::onCallVolumeDB(int type, int db) {
    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(job, jmid_valumedb, db);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("call onCallComplete worng");
            }
            return;
        }
        jniEnv->CallVoidMethod(job, jmid_valumedb, db);
        javaVm->DetachCurrentThread();
    }
}