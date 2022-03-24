//
// Created by mac on 2021/12/10.
//
#include "jni.h"
#include "string.h"
#include "AudioCallJava.h"
#include "AudioPlayState.h"
#include "FFmpegPlay.h"

JavaVM *jvm;
//音频处理和入队
FFmpegPlay *ffmpegplay;
//音频播放状态回调
AudioCallJava *audioCallJava;
//音频播放状态
AudioPlayState *audioPlayState;
//是否正在退出音频播放
bool quit = false;

//线程同步工具锁，保证播放中多线程操作可能导致的异常
pthread_mutex_t mutexPacket;

/**
 * 即将播放的音频资源，url或本地文件
 **/
const char *source;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *revered) {
    jvm = vm;
    JNIEnv *env;
    pthread_mutex_init(&mutexPacket, NULL);
    if (jvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_6;
}
extern "C"
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    pthread_mutex_destroy(&mutexPacket);
}
/**
 * 设置音频播放源
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1setSource(JNIEnv *env, jobject thiz, jstring _source) {
    source = env->GetStringUTFChars(_source, 0);
}

/**
 * 设置音频开始播放
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1start(JNIEnv *env, jobject thiz) {
    pthread_mutex_lock(&mutexPacket);
    if (ffmpegplay == NULL) {
        audioCallJava = new AudioCallJava(jvm, env, &thiz);
        audioPlayState = new AudioPlayState();
        ffmpegplay = new FFmpegPlay(audioPlayState, audioCallJava, source);
        ffmpegplay->prepare();
    }
    pthread_mutex_unlock(&mutexPacket);
}
/**
 * 设置音频暂停播放
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1pause(JNIEnv *env, jobject thiz) {
    pthread_mutex_lock(&mutexPacket);
    if (ffmpegplay != NULL) {
        ffmpegplay->pause();
    }
    pthread_mutex_unlock(&mutexPacket);
}
/**
 * 设置音频恢复播放
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1resume(JNIEnv *env, jobject thiz) {
    pthread_mutex_lock(&mutexPacket);
    if (ffmpegplay != NULL) {
        ffmpegplay->resume();
    }
    pthread_mutex_unlock(&mutexPacket);

}
/**
 * 设置音频播放进度
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1seek(JNIEnv *env, jobject thiz, jint seconds) {
    pthread_mutex_lock(&mutexPacket);
    if (ffmpegplay != NULL) {
        ffmpegplay->seek(seconds);
    }
    pthread_mutex_unlock(&mutexPacket);
}
/**
 * 设置音频播放结束并释放全部资源
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1stop(JNIEnv *env, jobject thiz) {
    pthread_mutex_lock(&mutexPacket);
    if (quit) {
        return;
    }

    jclass clz = env->GetObjectClass(thiz);
    jmethodID jmid_over = env->GetMethodID(clz, "releaseOver", "()V");

    quit = true;
    if (ffmpegplay != NULL) {
        ffmpegplay->release();
        delete (ffmpegplay);
        ffmpegplay = NULL;
        if (audioCallJava != NULL) {
            delete (audioCallJava);
            audioCallJava = NULL;
        }
        if (audioPlayState != NULL) {
            delete (audioPlayState);
            audioPlayState = NULL;
        }
    }
    quit = false;
    env->CallVoidMethod(thiz, jmid_over);
    pthread_mutex_unlock(&mutexPacket);
}
/**
 * 设置音频声音大小
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1volume(JNIEnv *env, jobject thiz, jint percent) {
    if (ffmpegplay != NULL) {
        ffmpegplay->setVolume(percent);
    }
}
/**
 * 设置音频播放声道
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1mute(JNIEnv *env, jobject thiz, jint mute) {
    if (ffmpegplay != NULL) {
        ffmpegplay->setMute(mute);
    }
}
/**
 * 设置音频音调
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1pitch(JNIEnv *env, jobject thiz, jfloat pitch) {
    if (ffmpegplay != NULL) {
        ffmpegplay->setPitch(pitch);
    }
}
/**
 * 设置音频播放速度
 * **/
extern "C"
JNIEXPORT void JNICALL
Java_media_share_audioplayer_AudioEngine_n_1speed(JNIEnv *env, jobject thiz, jfloat speed) {
    if (ffmpegplay != NULL) {
        ffmpegplay->setSpeed(speed);
    }
}
/**
 * 获取音频总时长
 * **/
extern "C"
JNIEXPORT int JNICALL
Java_media_share_audioplayer_AudioEngine_n_1duration(JNIEnv *env, jobject thiz) {
    if (ffmpegplay != NULL) {
        return ffmpegplay->duration;
    }
    return 0;
}