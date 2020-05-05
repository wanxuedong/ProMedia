//
// Created by simpo on 2020/4/14.
// 实现对音频的一系列操作，包括重采样，播放，暂停，停止等
//

#ifndef PROMUSIC_WLAUDIO_H
#define PROMUSIC_WLAUDIO_H


#include <linux/stddef.h>
#include "WlQueue.h"
#include "WlCallJava.h"
#include "SoundTouch.h"

extern "C" {
#include "libavcodec/avcodec.h"
#include <libswresample/swresample.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <libavutil/time.h>
};

using namespace soundtouch;

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

    //声音大小控制
    int volumePercent = 100;
    //声道控制
    int mute = 2;

    float pitch = 1.0f;
    float speed = 1.0f;

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

    pthread_mutex_t codecMutex;

    //SoundTouch
    SoundTouch *soundTouch = NULL;
    SAMPLETYPE *sampleBuffer = NULL;
    bool finished = true;
    uint8_t *out_buffer = NULL;
    int nb = 0;
    int num = 0;

    int const INETRVAL_TIME = 1000 * 100;

public:

    WlAudio(WlPlaystatus *playstatus, int sample_rate, WlCallJava *callJava);

    ~WlAudio();

    void play();

    int resampleAudio(void **pcmbuf);

    void initOpenSLES();

    int getCurrentSampleRateForOpensles(int sample_rate);

    void pause();

    void resume();

    void stop();

    void release();

    void setVolume(int percent);

    void setMute(int mute);

    int getSoundTouchData();

    void setPitch(float pitch);

    void setSpeed(float speed);

    int getPcmDb(char *pcmcata, size_t pcmsize);

};


#endif
