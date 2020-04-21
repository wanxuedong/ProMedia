#include <jni.h>
#include <string>
#include "WlCallJava.h"
#include "WlFFmpeg.h"


WlCallJava *wlCallJava;
JavaVM *javaVm;
WlFFmpeg *fFmpeg;
WlPlaystatus *playstatus = NULL;

bool nexit = true;
pthread_t thread_start;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    javaVm = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) env, JNI_VERSION_1_6) != JNI_OK) {
        return result;
    }
    return JNI_VERSION_1_6;
};

extern "C"
JNIEXPORT void JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1prepared(JNIEnv *env, jobject instance,
                                                             jstring source_) {

    const char *source = env->GetStringUTFChars(source_, 0);
    if (fFmpeg == NULL) {
        if (wlCallJava == NULL) {
            wlCallJava = new WlCallJava(javaVm, env, &instance);
        }
        playstatus = new WlPlaystatus();
        fFmpeg = new WlFFmpeg(playstatus, wlCallJava, source);
        fFmpeg->prepared();
    }


}

void *startCallBack(void *data) {
    WlFFmpeg *fFmpeg = (WlFFmpeg *) data;
    fFmpeg->start();
    pthread_exit(&thread_start);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1start(JNIEnv *env, jobject thiz) {
    if (fFmpeg != NULL) {
        pthread_create(&thread_start, NULL, startCallBack, fFmpeg);
    }
}extern "C"
JNIEXPORT void JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1pause(JNIEnv *env, jobject thiz) {
    if (fFmpeg != NULL) {
        fFmpeg->pause();
    }

}extern "C"
JNIEXPORT void JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1resume(JNIEnv *env, jobject thiz) {
    if (fFmpeg != NULL) {
        fFmpeg->resume();
    }
}extern "C"
JNIEXPORT void JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1stop(JNIEnv *env, jobject thiz) {

    if (!nexit) {
        return;
    }

    jclass clz = env->GetObjectClass(thiz);
    jmethodID jmid_next = env->GetMethodID(clz, "onCallNext", "()V");
    nexit = false;
    if (fFmpeg != NULL) {
        fFmpeg->release();
        delete (fFmpeg);
        fFmpeg = NULL;
        if (wlCallJava != NULL) {
            delete (wlCallJava);
            wlCallJava = NULL;
        }
        if (playstatus != NULL) {
            delete (playstatus);
            playstatus = NULL;
        }
    }
    nexit = true;
    env->CallVoidMethod(thiz, jmid_next);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1seek(JNIEnv *env, jobject thiz, jint secds) {
    if (fFmpeg != NULL) {
        fFmpeg->seek(secds);
    }
}

extern "C"
JNIEXPORT int JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1duration(JNIEnv *env, jobject thiz) {
    if (fFmpeg != NULL) {
        return fFmpeg->duration;
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1volume(JNIEnv *env, jobject thiz,
                                                           jint percent) {
    if (fFmpeg != NULL) {
        fFmpeg->setVolume(percent);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simpo_promusic_music_player_MusicPlayer_n_1mute(JNIEnv *env, jobject thiz, jint mute) {
    if(fFmpeg != NULL)
    {
        fFmpeg->setMute(mute);
    }
}