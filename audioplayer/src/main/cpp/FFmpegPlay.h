//
// Created by mac on 2021/12/10.
// 音频处理和入队
//

#ifndef TEST_FFMPEGPLAY_H
#define TEST_FFMPEGPLAY_H


#include "OpenSLESPlay.h"
#include "AudioPlayState.h"
#include "AudioCallJava.h"
#include "pthread.h"

extern "C"
{
#include "libavformat/avformat.h"
#include <libavutil/time.h>
};

class FFmpegPlay {

public:

    //音频播放状态
    AudioPlayState *playState = NULL;
    //音频播放状态回调
    AudioCallJava *audioCallJava;
    //音频播放源
    const char *source = NULL;
    //音频解码线程
    pthread_t decodeThread;
    //音频开始入队线程
    pthread_t thread_start;
    //音频上下文
    AVFormatContext *pFormatCtx = NULL;
    //播放音频源控制器
    OpenSLESPlay *audioPlay = NULL;
    //音频初始化同步锁
    pthread_mutex_t init_mutex;
    bool exit = false;
    int duration = 0;
    //音频调整进度同步锁
    pthread_mutex_t seek_mutex;

public:

    FFmpegPlay(AudioPlayState *audioPlayState, AudioCallJava *audioCallJava, const char *source);

    void prepare();

    void startDeCode();

    void pause();

    void resume();

    void seek(int64_t secds);

    void decodeAudioThread();

    void release();

    void setVolume(int percent);

    void setMute(int mute);

    void setPitch(float pitch);

    void setSpeed(float speed);

    ~FFmpegPlay();

};


#endif //TEST_FFMPEGPLAY_H
