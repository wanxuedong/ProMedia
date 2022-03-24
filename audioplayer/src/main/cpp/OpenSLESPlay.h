//
// Created by mac on 2021/12/10.
// OpenSLES播放音频源
//

#ifndef TEST_OPENSLESPLAY_H
#define TEST_OPENSLESPLAY_H

#include "AudioQueue.h"
#include "AudioCallJava.h"
#include "AudioPlayState.h"
#include "SoundTouch.h"
#include<cmath>

using namespace soundtouch;

extern "C"
{
#include "libavcodec/avcodec.h"
#include <libswresample/swresample.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};


class OpenSLESPlay {

public:
    //音频流索引
    int streamIndex = -1;
    //音频上下文
    AVCodecContext *avCodecContext = NULL;
    //音频参数结构
    AVCodecParameters *codecPar = NULL;
    //音频数据存储生产者消费者队列
    AudioQueue *queue = NULL;
    //音频播放状态
    AudioPlayState *playState = NULL;
    //java回调
    AudioCallJava *callJava = NULL;

    pthread_t thread_play;
    AVPacket *avPacket = NULL;
    AVFrame *avFrame = NULL;
    int ret = 0;
    //重采样后的音频buffer数据
    uint8_t *buffer = NULL;
    //采样个数
    int data_size = 0;
    //采样率
    int sample_rate = 0;

    //当前播放时长
    double clock;
    //音频总时长
    int duration = 0;
    AVRational time_base;
    //当前frame时间
    double now_time;
    //上一次调用时间
    double last_time;


    // 引擎接口
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine = NULL;

    //混音器
    SLObjectItf outputMixObject = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    //pcm
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;
    //声音控制
    SLVolumeItf pcmVolumePlay = NULL;
    //声道控制
    SLMuteSoloItf pcmMutePlay = NULL;

    //当前音量
    int volumePercent = 100;
    //当前声道，0右声道，1左声道，2立体声
    int mute = 2;

    //缓冲器队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue = NULL;

    //使用soundTouch需要注意STTypes.h里面设置是16位还是32位
    SoundTouch *soundTouch = NULL;
    SAMPLETYPE *sampleBuffer = NULL;
    bool finished = true;
    uint8_t *out_buffer = NULL;
    int nb = 0;
    int num = 0;
    //音调，正常为1.0f
    float pitch = 1.0f;
    //音速，正常为1.0f
    float speed = 1.0f;

public:
    OpenSLESPlay(AudioPlayState *playState, int sample_rate, AudioCallJava *callJava);

    ~OpenSLESPlay();

    void play();

    int resampleAudio(void **pcmbuf);

    void initOpenSLES();

    int getCurrentSampleRateForOpenSles(int sample_rate);

    void pause();

    void resume();

    void stop();

    void release();

    void setVolume(int percent);

    void setMute(int mute);

    void setPitch(float pitch);

    void setSpeed(float speed);

    int getPcmDB(char *pcmcata, size_t pcmsize);

    int getSoundTouchData();

};


#endif //TEST_OPENSLESPLAY_H
