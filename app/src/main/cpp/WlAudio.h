//
// Created by simpo on 2020/4/14.
// 实现对音频的一系列操作，包括重采样，播放，暂停，停止等
//

#ifndef PROMUSIC_WLAUDIO_H
#define PROMUSIC_WLAUDIO_H


#include <linux/stddef.h>
#include "WlQueue.h"
#include "WlCallJava.h"

extern "C" {
#include "libavcodec/avcodec.h"
#include <libswresample/swresample.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class WlAudio {

public:

    int streamIndex = -1;
    AVCodecContext *avCodecContext = NULL;
    AVCodecParameters *codecpar = NULL;
    WlQueue *queue = NULL;
    WlPlaystatus *playstatus = NULL;
    WlCallJava *callJava = NULL;

    pthread_t thread_play;
    AVPacket *avPacket = NULL;
    AVFrame *avFrame = NULL;
    int ret = 0;
    uint8_t *buffer = NULL;
    int data_size = 0;
    int sample_rate = 0;

    AVRational time_base;
    double clock;//当前播放时长
    int duration = 0; //总的播放时间
    double now_time;//当前frame时间
    double last_time; //上一次调用时间

    int volumePercent = 100;
    int mute = 2;

    // 引擎接口
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine = NULL;

    //混音器
    SLObjectItf outputMixObject = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    //播放，音量，声道控制
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;
    SLVolumeItf pcmVolumePlay = NULL;
    SLMuteSoloItf pcmMutePlay = NULL;

    //缓冲器队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue = NULL;

public:

    WlAudio(WlPlaystatus *playstatus, int sample_rate, WlCallJava *callJava);

    ~WlAudio();

    void play();

    int resampleAudio();

    void initOpenSLES();

    int getCurrentSampleRateForOpensles(int sample_rate);

    void pause();

    void resume();

    void stop();

    void release();

    void setVolume(int percent);

    void setMute(int mute);

};


#endif
