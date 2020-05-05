#include <stdint.h>
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
    jmid_renderyuv = jniEnv->GetMethodID(jcls, "onCallRenderYUV", "(II[B[B[B)V");
    jmid_supportvideo = jniEnv->GetMethodID(jcls, "onCallIsSupportMediaCodec","(Ljava/lang/String;)Z");
    jmid_initmediacodec = jniEnv->GetMethodID(jcls, "initMediaCodec","(Ljava/lang/String;II[B[B)V");
    jmid_decodeavpacket = jniEnv->GetMethodID(jcls, "decodeAVPacket", "(I[B)V");

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

void WlCallJava::onCallRenderYUV(int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv) {

    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
        return;
    }

    jbyteArray y = jniEnv->NewByteArray(width * height);
    jniEnv->SetByteArrayRegion(y, 0, width * height, reinterpret_cast<const jbyte *>(fy));

    jbyteArray u = jniEnv->NewByteArray(width * height / 4);
    jniEnv->SetByteArrayRegion(u, 0, width * height / 4, reinterpret_cast<const jbyte *>(fu));

    jbyteArray v = jniEnv->NewByteArray(width * height / 4);
    jniEnv->SetByteArrayRegion(v, 0, width * height / 4, reinterpret_cast<const jbyte *>(fv));

    jniEnv->CallVoidMethod(job, jmid_renderyuv, width, height, y, u, v);

    jniEnv->DeleteLocalRef(y);
    jniEnv->DeleteLocalRef(u);
    jniEnv->DeleteLocalRef(v);

    javaVm->DetachCurrentThread();
}

bool WlCallJava::onCallIsSupportVideo(const char *ffcodecname) {

    bool support = false;
    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
        return support;
    }

    jstring type = jniEnv->NewStringUTF(ffcodecname);
    support = jniEnv->CallBooleanMethod(job, jmid_supportvideo, type);
    jniEnv->DeleteLocalRef(type);
    javaVm->DetachCurrentThread();
    return support;
}

void WlCallJava::onCallInitMediacodec(const char *mime, int width, int height, int csd0_size,
                                      int csd1_size, uint8_t *csd_0, uint8_t *csd_1) {

    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
    }

    jstring type = jniEnv->NewStringUTF(mime);
    jbyteArray csd0 = jniEnv->NewByteArray(csd0_size);
    jniEnv->SetByteArrayRegion(csd0, 0, csd0_size, reinterpret_cast<const jbyte *>(csd_0));
    jbyteArray csd1 = jniEnv->NewByteArray(csd1_size);
    jniEnv->SetByteArrayRegion(csd1, 0, csd1_size, reinterpret_cast<const jbyte *>(csd_1));

    jniEnv->CallVoidMethod(job, jmid_initmediacodec, type, width, height, csd0, csd1);

    jniEnv->DeleteLocalRef(csd0);
    jniEnv->DeleteLocalRef(csd1);
    jniEnv->DeleteLocalRef(type);
    javaVm->DetachCurrentThread();

}

void WlCallJava::onCallDecodeAVPacket(int datasize, uint8_t *packetdata) {
    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
    }
    jbyteArray data = jniEnv->NewByteArray(datasize);
    jniEnv->SetByteArrayRegion(data, 0, datasize, reinterpret_cast<const jbyte *>(packetdata));
    jniEnv->CallVoidMethod(job, jmid_decodeavpacket, datasize, data);
    jniEnv->DeleteLocalRef(data);
    javaVm->DetachCurrentThread();

}