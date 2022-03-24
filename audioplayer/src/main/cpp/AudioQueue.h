//
// Created by yangw on 2018-3-6.
//

#ifndef MYMUSIC_WLQUEUE_H
#define MYMUSIC_WLQUEUE_H

#include "jni.h"
#include "queue"
#include "pthread.h"
#include "AndroidLog.h"
#include "AudioPlayState.h"

extern "C"
{
#include "libavcodec/avcodec.h"
};


class AudioQueue {

public:
    std::queue<AVPacket *> queuePacket;
    //线程同步工具锁
    pthread_mutex_t mutexPacket;
    //线程同步信号量
    pthread_cond_t condPacket;
    AudioPlayState *playState = NULL;
    const int MAX_SIZE = 40;

public:

    AudioQueue(AudioPlayState *playState);
    ~AudioQueue();

    int putAvPacket(AVPacket *packet);
    int getAvPacket(AVPacket *packet);

    int getQueueSize();

    bool isFull();

    void clearAvPacket();


};


#endif //MYMUSIC_WLQUEUE_H
