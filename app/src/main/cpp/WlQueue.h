//
// Created by simpo on 2020/4/15.
// 使用队列存储FFmpeg解码后的数据，存取分离，保证系统流畅性
//

#ifndef PROMUSIC_WLQUEUE_H
#define PROMUSIC_WLQUEUE_H

#include <pthread.h>
#include "queue"
#include "WlPlaystatus.h"
#include "AndroidLog.h"

extern "C" {
#include <libavcodec/avcodec.h>
};

class WlQueue {

public:

    std::queue<AVPacket *> queuePacket;
    pthread_mutex_t pthreadMutex;
    pthread_cond_t pthreadCond;
    WlPlaystatus *wlPlaystatus;

public:
    WlQueue(WlPlaystatus *wlPlaystatus);

    ~WlQueue();

    int putAvPacket(AVPacket *avPacket);

    int getAvPacket(AVPacket *avPacket);

    void clearAvPacket();

    int getQueueSize();

    void noticeQueue();

};


#endif
