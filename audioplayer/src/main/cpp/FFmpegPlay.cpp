//
// Created by mac on 2021/12/10.
//

#include "FFmpegPlay.h"
#include "OpenSLESPlay.h"

FFmpegPlay::FFmpegPlay(AudioPlayState *_playState, AudioCallJava *_audioCallJava,
                       const char *_source) {
    playState = _playState;
    audioCallJava = _audioCallJava;
    source = _source;
    exit = false;
    pthread_mutex_init(&init_mutex, NULL);
    pthread_mutex_init(&seek_mutex, NULL);
}

/**
 * 音频解码线程
 * **/
void *decodeAudio(void *data) {
    FFmpegPlay *play = (FFmpegPlay *) data;
    play->decodeAudioThread();
    pthread_exit(&play->decodeThread);
}

/**
 * 加载音频
 * **/
void FFmpegPlay::prepare() {
    pthread_create(&decodeThread, NULL, decodeAudio, this);
}

/**
 * 开始解码并入队
 * **/
void FFmpegPlay::startDeCode() {
    if (audioPlay == NULL) {
        return;
    }
    //初始化播放器并准备播放
    audioPlay->play();
    while (playState != NULL && !playState->exit) {

        //调整进度过程中暂停解码
        if (playState->seek) {
            continue;
        }

        //设置队列缓存大小
        if (audioPlay->queue->isFull()) {
            continue;
        }
        //通过音频流索引读取音频数据并入队
        AVPacket *avPacket = av_packet_alloc();
        if (av_read_frame(pFormatCtx, avPacket) == 0) {
            if (avPacket->stream_index == audioPlay->streamIndex) {
                audioPlay->queue->putAvPacket(avPacket);
            } else {
                av_packet_free(&avPacket);
                av_free(avPacket);
            }
        } else {
            av_packet_free(&avPacket);
            av_free(avPacket);
            //虽然解码结束，但是队列中依然存在数据，继续保持循环状态
            while (playState != NULL && !playState->exit) {
                if (audioPlay->queue->getQueueSize() > 0) {
                    continue;
                } else {
                    playState->exit = true;
                    break;
                }
            }
            break;
        }
    }
    exit = true;
    //音频解码并播放结束
    if (audioCallJava != NULL) {
        audioCallJava->onCallComplete(CHILD_THREAD);
    }
}

/**
 * 音频开始解码入队
 * **/
void *start(void *data) {
    FFmpegPlay *play = (FFmpegPlay *) data;
    play->startDeCode();
    pthread_exit(&play->thread_start);
}

void FFmpegPlay::pause() {
    if (audioPlay != NULL) {
        audioPlay->pause();
    }
}

void FFmpegPlay::resume() {
    if (audioPlay != NULL) {
        audioPlay->resume();
    }
}

/**
 * 调整音频播放进度条
 * **/
void FFmpegPlay::seek(int64_t secds) {

    if (duration < 0) {
        return;
    }
    if (secds >= 0 && secds <= duration) {
        if (audioPlay != NULL) {
            //设置播放参数和状态
            pthread_mutex_lock(&seek_mutex);
            playState->seek = true;
            audioPlay->queue->clearAvPacket();
            audioPlay->clock = 0;
            audioPlay->last_time = 0;
            int64_t rel = secds * AV_TIME_BASE;
            //调整进度核心代码
            avformat_seek_file(pFormatCtx, -1, INT64_MIN, rel, INT64_MAX, 0);
            pthread_mutex_unlock(&seek_mutex);
            playState->seek = false;
        }
    }
}

/**
 * 读取音频资源超时回调
 * **/
int timeOutCallBack(void *ctx) {
    FFmpegPlay *play = (FFmpegPlay *) ctx;
    if (play->playState->exit) {
        if (LOG_DEBUG) {
            LOGE("av format open input too long to wait");
        }
        return AVERROR_EOF;
    }
    return 0;
}

/**
 * 真实的读取音频资源信息，并成功回调
 * **/
void FFmpegPlay::decodeAudioThread() {
    pthread_mutex_lock(&init_mutex);
    playState->load = true;
    if (audioCallJava != NULL) {
        audioCallJava->onCallLoad(CHILD_THREAD, true);
    }
    //注册解码器
    av_register_all();
    //初始化网络
    avformat_network_init();
    //初始化解码器上下文
    pFormatCtx = avformat_alloc_context();

    //打开多媒体文件超时回调
    pFormatCtx->interrupt_callback.callback = timeOutCallBack;
    //打开多媒体文件超时回调传参
    pFormatCtx->interrupt_callback.opaque = this;

    //打开多媒体文件
    if (avformat_open_input(&pFormatCtx, source, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGE("can not open url :%s", source);
        }
        if (audioCallJava != NULL) {
            audioCallJava->onCallError(CHILD_THREAD, 1001, (char *) "can not open url");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    //打开多媒体文件流
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGE("can not find streams from %s", source);
        }
        if (audioCallJava != NULL) {
            audioCallJava->onCallError(CHILD_THREAD, 1002,
                                       (char *) "can not find streams from url");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    //循环多媒体文件并找到音频流并存储相关参数
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO)//得到音频流
        {
            if (audioPlay == NULL) {
                audioPlay = new OpenSLESPlay(playState,
                                             pFormatCtx->streams[i]->codecpar->sample_rate,
                                             audioCallJava);
                audioPlay->streamIndex = i;
                audioPlay->codecPar = pFormatCtx->streams[i]->codecpar;
                audioPlay->duration = pFormatCtx->duration / AV_TIME_BASE;
                audioPlay->time_base = pFormatCtx->streams[i]->time_base;
                duration = audioPlay->duration;
            }
        }
    }
    if (audioPlay == NULL) {
        if (LOG_DEBUG) {
            LOGE("can not find audio stream");
        }
        return;
    }

    // 读取音频头部详细信息
    AVDictionaryEntry *tag = nullptr;
    while (tag = av_dict_get(pFormatCtx->metadata, "", tag, AV_DICT_IGNORE_SUFFIX)) {
        audioCallJava->onCallSoundInfo(CHILD_THREAD, tag->key, tag->value);
    }
    //找到对应的音频解码器
    AVCodec *dec = avcodec_find_decoder(audioPlay->codecPar->codec_id);
    if (!dec) {
        if (LOG_DEBUG) {
            LOGE("can not find decoder");
        }
        if (audioCallJava != NULL) {
            audioCallJava->onCallError(CHILD_THREAD, 1003, (char *) "can not find decoder");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    //初始化音频解码器
    audioPlay->avCodecContext = avcodec_alloc_context3(dec);
    if (!audioPlay->avCodecContext) {
        if (LOG_DEBUG) {
            LOGE("can not alloc new av code context");
        }
        if (audioCallJava != NULL) {
            audioCallJava->onCallError(CHILD_THREAD, 1004,
                                       (char *) "can not alloc new av_code_context");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    //填充音频解码器
    if (avcodec_parameters_to_context(audioPlay->avCodecContext, audioPlay->codecPar) < 0) {
        if (LOG_DEBUG) {
            LOGE("can not fill av code context");
        }
        if (audioCallJava != NULL) {
            audioCallJava->onCallError(CHILD_THREAD, 1005, (char *) "can not fill av_code_context");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    //打开音频文件
    if (avcodec_open2(audioPlay->avCodecContext, dec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGE("cant not open audioPlay streams");
        }
        if (audioCallJava != NULL) {
            audioCallJava->onCallError(CHILD_THREAD, 1006,
                                       (char *) "cant not open audioPlay streams");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    playState->load = false;
    if (audioCallJava != NULL) {
        audioCallJava->onCallLoad(CHILD_THREAD, false);
    }
    //成功读取音频文件信息，准备解码并播放
    if (audioCallJava != NULL) {
        if (playState != NULL && !playState->exit) {
            audioCallJava->onCallStart(CHILD_THREAD);
            pthread_create(&thread_start, NULL, start, this);
        } else {
            exit = true;
        }
    }
    pthread_mutex_unlock(&init_mutex);

}

/**
 * 释放资源
 * **/
void FFmpegPlay::release() {

    pthread_mutex_lock(&init_mutex);
    if (LOG_DEBUG) {
        LOGD("开始释放Ffmpeg");
    }
    playState->exit = true;
    int sleepCount = 0;
    while (!exit) {
        if (sleepCount > 1000) {
            exit = true;
        }
        if (LOG_DEBUG) {
            LOGD("wait ffmpeg  exit %d", sleepCount);
        }
        sleepCount++;
        av_usleep(1000 * 10);//暂停10毫秒
    }

    if (LOG_DEBUG) {
        LOGD("释放 Audio");
    }

    if (audioPlay != NULL) {
        audioPlay->release();
        delete (audioPlay);
        audioPlay = NULL;
    }

    if (LOG_DEBUG) {
        LOGD("释放 封装格式上下文");
    }
    if (pFormatCtx != NULL) {
        avformat_close_input(&pFormatCtx);
        avformat_free_context(pFormatCtx);
        pFormatCtx = NULL;
    }
    if (LOG_DEBUG) {
        LOGD("释放 callJava");
    }
    if (audioCallJava != NULL) {
        audioCallJava = NULL;
    }
    if (LOG_DEBUG) {
        LOGD("释放 playState");
    }
    if (playState != NULL) {
        playState = NULL;
    }
    pthread_mutex_unlock(&init_mutex);
}

/**
 * 设置音量
 * **/
void FFmpegPlay::setVolume(int percent) {
    if (audioPlay != NULL) {
        audioPlay->setVolume(percent);
    }
}

/**
 * 设置声道
 * **/
void FFmpegPlay::setMute(int mute) {
    if (audioPlay != NULL) {
        audioPlay->setMute(mute);
    }
}

/**
 * 设置播放音调，正常为1.0f
 * **/
void FFmpegPlay::setPitch(float pitch) {
    if (audioPlay != NULL) {
        audioPlay->setPitch(pitch);
    }
}

/**
 * 设置播放速度，正常为1.0f
 * **/
void FFmpegPlay::setSpeed(float speed) {
    if (audioPlay != NULL) {
        audioPlay->setSpeed(speed);
    }
}

FFmpegPlay::~FFmpegPlay() {
    pthread_mutex_destroy(&init_mutex);
    pthread_mutex_destroy(&seek_mutex);
}
