#ifndef PROMUSIC_WLFFMPEG_H
#define PROMUSIC_WLFFMPEG_H

#include "pthread.h"
#include "WlCallJava.h"
#include "WlAudio.h"
#include "AndroidLog.h"
//
// Created by simpo on 2020/4/15.
// FFmpeg实际操作类，包含数据初始化，解码入队
//

extern "C"
{
#include "libavformat/avformat.h"
#include <libavutil/time.h>
};


class WlFFmpeg {

public:

    WlCallJava *wlCallJava = NULL;
    const char *url = NULL;
    pthread_t decodeThread;
    AVFormatContext *pFormatCtx;
    WlAudio *audio = NULL;
    WlPlaystatus *playstatus = NULL;
    pthread_mutex_t init_mutex;
    bool exit = false;
    int duration = 0;
    pthread_mutex_t seek_mutex;

public:

    WlFFmpeg(WlPlaystatus *playstatus, WlCallJava *wlCallJava, const char *url);

    ~WlFFmpeg();

    void prepared();

    void decodeFFmpegThread();

    void start();

    void pause();

    void resume();

    void release();

    void seek(int64_t secds);
};


#endif
