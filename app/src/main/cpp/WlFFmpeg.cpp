

#include "WlFFmpeg.h"

WlFFmpeg::WlFFmpeg(WlPlaystatus *playstatus, WlCallJava *wlCallJava, const char *url) {
    this->playstatus = playstatus;
    this->wlCallJava = wlCallJava;
    this->url = url;
    exit = false;
    pthread_mutex_init(&init_mutex, NULL);
    pthread_mutex_init(&seek_mutex, NULL);
}

void *decodeFFmpeg(void *data) {
    WlFFmpeg *wlFFmpeg = (WlFFmpeg *) data;
    wlFFmpeg->decodeFFmpegThread();
    pthread_exit(&wlFFmpeg->decodeThread);
}

void WlFFmpeg::prepared() {
    //初始化音频信息分析线程
    pthread_create(&decodeThread, NULL, decodeFFmpeg, this);

}

int avformat_callback(void *ctx) {
    WlFFmpeg *fFmpeg = (WlFFmpeg *) ctx;
    if (fFmpeg->playstatus->exit) {
        return AVERROR_EOF;
    }
    return 0;
}

/**
 * 音频工作初始化阶段
 * **/
void WlFFmpeg::decodeFFmpegThread() {

    pthread_mutex_lock(&init_mutex);
    //初始化ffmpeg
    av_register_all();
    //初始化网络协议
    avformat_network_init();
    //分配空间
    pFormatCtx = avformat_alloc_context();

    //添加错误回调处理
    pFormatCtx->interrupt_callback.callback = avformat_callback;
    pFormatCtx->interrupt_callback.opaque = this;

    //打开资源并读取文件头部信息
    if (avformat_open_input(&pFormatCtx, url, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGE("can not open url :%s", url);
        }
        wlCallJava->onCallError(CHILD_THREAD, 1001, "can not open url");
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    //从资源流中读取信息，预防没有头部信息的资源
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGE("can not find streams from %s", url);
        }
        wlCallJava->onCallError(CHILD_THREAD, 1002, "can not find streams from url");
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    //遍历资源中的音频流
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            if (audio == NULL) {
                //将音频流相关数据存储起来
                audio = new WlAudio(playstatus, pFormatCtx->streams[i]->codecpar->sample_rate,
                                    wlCallJava);
                audio->streamIndex = i;
                audio->codecpar = pFormatCtx->streams[i]->codecpar;
                //获取到文件时长需要除以AV_TIME_BASE，才能得到我们需要的时间，AV_TIME_BASE一般为1000000，单位微妙
                //pFormatCtx->duration的单位也是微妙，二者相除，返回单位为秒
                audio->duration = pFormatCtx->duration / AV_TIME_BASE;
                //为了保证数据的准确性，采用了分子分母的形式存储了时间基，单位秒
                audio->time_base = pFormatCtx->streams[i]->time_base;
                duration = audio->duration;
            }
        }
    }
    //找出指定的音频解码器
    AVCodec *dec = avcodec_find_decoder(audio->codecpar->codec_id);
    if (!dec) {
        if (LOG_DEBUG) {
            LOGE("can not find decoder");
        }
        wlCallJava->onCallError(CHILD_THREAD, 1003, "can not find decoder");
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    //分配音频流空间
    audio->avCodecContext = avcodec_alloc_context3(dec);
    if (!audio->avCodecContext) {
        if (LOG_DEBUG) {
            LOGE("can not alloc new decodecctx");
        }
        wlCallJava->onCallError(CHILD_THREAD, 1004, "can not alloc new decodecctx");
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    if (avcodec_parameters_to_context(audio->avCodecContext, audio->codecpar) < 0) {
        if (LOG_DEBUG) {
            LOGE("can not fill decodecctx");
        }
        wlCallJava->onCallError(CHILD_THREAD, 1005, "ccan not fill decodecctx");
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    //初始化AVCodecContext
    if (avcodec_open2(audio->avCodecContext, dec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGE("cant not open audio strames");
        }
        wlCallJava->onCallError(CHILD_THREAD, 1006, "cant not open audio strames");
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    //回调java层，表示准备工作完成
    if (wlCallJava != NULL) {
        if (playstatus != NULL && !playstatus->exit) {
            wlCallJava->onCallPrepared(CHILD_THREAD);
        } else {
            exit = true;
        }
    }
    pthread_mutex_unlock(&init_mutex);
}

/**
 * 开始音频的解码
 * **/
void WlFFmpeg::start() {
    if (audio == NULL) {
        return;
    }
    audio->play();

    //循环一帧一帧读取音频的数据
    while (playstatus != NULL && !playstatus->exit) {

        if (playstatus->seek) {
            continue;
        }

        if (audio->queue->getQueueSize() > 40) {
            continue;
        }

        AVPacket *avPacket = av_packet_alloc();
        //读取流下一帧信息
        if (av_read_frame(pFormatCtx, avPacket) == 0) {
            //一帧一帧的获取音频的数据
            if (avPacket->stream_index == audio->streamIndex) {
                audio->queue->putAvPacket(avPacket);
            } else {
                av_packet_free(&avPacket);
                av_free(avPacket);
            }

        } else {
            av_packet_free(&avPacket);
            av_free(avPacket);
            while (playstatus != NULL && !playstatus->exit) {
                if (audio->queue->getQueueSize() > 0) {
                    continue;
                } else {
                    playstatus->exit = true;
                    break;
                }
            }
            break;
        }
    }
    if (wlCallJava != NULL) {
        wlCallJava->onCallComplete(CHILD_THREAD);
    }
    exit = true;
}


void WlFFmpeg::pause() {
    if (audio != NULL) {
        audio->pause();
    }
}

void WlFFmpeg::resume() {
    if (audio != NULL) {
        audio->resume();
    }
}

void WlFFmpeg::release() {

    if (LOG_DEBUG) {
        LOGE("开始释放FFmpeg");
    }
    playstatus->exit = true;
    pthread_mutex_lock(&init_mutex);
    int sleepCount = 0;
    while (!exit) {
        if (sleepCount > 1000) {
            exit = true;
        }
        if (LOG_DEBUG) {
            LOGE("wait ffmpeg  exit %d", sleepCount);
        }
        sleepCount++;
        av_usleep(1000 * 10);//暂停10毫秒
    }

    if (LOG_DEBUG) {
        LOGE("释放 Audio");
    }

    if (audio != NULL) {
        audio->stop();
        audio->release();
        delete (audio);
        audio = NULL;
    }

    if (LOG_DEBUG) {
        LOGE("释放 封装格式上下文");
    }
    if (pFormatCtx != NULL) {
        avformat_close_input(&pFormatCtx);
        avformat_free_context(pFormatCtx);
        pFormatCtx = NULL;
    }
    if (LOG_DEBUG) {
        LOGE("释放 callJava");
    }
    if (wlCallJava != NULL) {
        wlCallJava = NULL;
    }
    if (LOG_DEBUG) {
        LOGE("释放 playstatus");
    }
    if (playstatus != NULL) {
        playstatus = NULL;
    }
    pthread_mutex_unlock(&init_mutex);
}

WlFFmpeg::~WlFFmpeg() {

    pthread_mutex_destroy(&init_mutex);
    pthread_mutex_destroy(&seek_mutex);

}

void WlFFmpeg::seek(int64_t secds) {

    if (duration <= 0) {
        return;
    }
    if (secds >= 0 && secds <= duration) {
        if (audio != NULL) {
            playstatus->seek = true;
            audio->queue->clearAvPacket();
            audio->clock = 0;
            audio->last_time = 0;
            pthread_mutex_lock(&seek_mutex);
            int64_t rel = secds * AV_TIME_BASE;
            avformat_seek_file(pFormatCtx, -1, INT64_MIN, rel, INT64_MAX, 0);
            pthread_mutex_unlock(&seek_mutex);
            playstatus->seek = false;
        }
    }
}
