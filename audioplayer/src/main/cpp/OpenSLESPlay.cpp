//
// Created by mac on 2021/12/10.
//

#include "OpenSLESPlay.h"

OpenSLESPlay::OpenSLESPlay(AudioPlayState *playState, int sample_rate, AudioCallJava *callJava) {
    this->callJava = callJava;
    this->playState = playState;
    this->sample_rate = sample_rate;
    queue = new AudioQueue(playState);
    buffer = (uint8_t *) av_malloc(sample_rate * 2 * 2);
    sampleBuffer = static_cast<SAMPLETYPE *>(malloc(sample_rate * 2 * 2));
    soundTouch = new SoundTouch();
    soundTouch->setChannels(2);
    soundTouch->setSampleRate(sample_rate);
    soundTouch->setPitch(pitch);
    soundTouch->setTempo(speed);
}

void *decodePlay(void *data) {
    OpenSLESPlay *play = (OpenSLESPlay *) data;

    play->initOpenSLES();

    pthread_exit(&play->thread_play);
}

void OpenSLESPlay::play() {

    pthread_create(&thread_play, NULL, decodePlay, this);

}

/**
 * 读取音频数据
 * 返回采样个数
 * **/
int OpenSLESPlay::resampleAudio(void **pcmbuf) {
    data_size = 0;
    while (playState != NULL && !playState->exit) {

        if (playState->seek) {
            continue;
        }

        if (queue->getQueueSize() == 0) {
            //加载中
            if (!playState->loading) {
                playState->loading = true;
                callJava->onCallLoading(CHILD_THREAD, true);
            }
            continue;
        } else {
            if (playState->loading) {
                playState->loading = false;
                callJava->onCallLoading(CHILD_THREAD, false);
            }
        }
        avPacket = av_packet_alloc();
        if (queue->getAvPacket(avPacket) != 0) {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            continue;
        }
        ret = avcodec_send_packet(avCodecContext, avPacket);
        if (ret != 0) {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            continue;
        }
        avFrame = av_frame_alloc();
        ret = avcodec_receive_frame(avCodecContext, avFrame);
        if (ret == 0) {

            //获取通道布局
            if (avFrame->channels && avFrame->channel_layout == 0) {
                avFrame->channel_layout = av_get_default_channel_layout(avFrame->channels);
            } else if (avFrame->channels == 0 && avFrame->channel_layout > 0) {
                avFrame->channels = av_get_channel_layout_nb_channels(avFrame->channel_layout);
            }

            SwrContext *swr_ctx;

            //初始化重采样
            swr_ctx = swr_alloc_set_opts(
                    NULL,
                    AV_CH_LAYOUT_STEREO,
                    AV_SAMPLE_FMT_S16,
                    avFrame->sample_rate,
                    avFrame->channel_layout,
                    (AVSampleFormat) avFrame->format,
                    avFrame->sample_rate,
                    NULL, NULL
            );
            if (!swr_ctx || swr_init(swr_ctx) < 0) {
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
                av_frame_free(&avFrame);
                av_free(avFrame);
                avFrame = NULL;
                swr_free(&swr_ctx);
                continue;
            }

            //进行重采样
            nb = swr_convert(
                    swr_ctx,
                    &buffer,
                    avFrame->nb_samples,
                    (const uint8_t **) avFrame->data,
                    avFrame->nb_samples);

            int out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
            data_size = nb * out_channels * av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);

            //获取播放时间进度
            now_time = avFrame->pts * av_q2d(time_base);
            if (clock < duration) {
                //防止因异常数据导致clock的值巨大，导致返回的进度时间异常
                if (now_time < clock) {
                    now_time = clock;
                }
            }
            clock = now_time;
            *pcmbuf = buffer;

            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            swr_free(&swr_ctx);
            break;
        } else {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            continue;
        }
    }
    return data_size;
}

/**
 * 获取经过SoundTouch处理后的数据
 * 返回采样个数
 * **/
int OpenSLESPlay::getSoundTouchData() {

    while (playState != NULL && !playState->exit) {
        out_buffer = NULL;
        if (finished) {
            finished = false;
            data_size = resampleAudio(reinterpret_cast<void **>(&out_buffer));
            if (data_size > 0) {
                for (int i = 0; i < data_size / 2 + 1; i++) {
                    sampleBuffer[i] = (out_buffer[i * 2] | ((out_buffer[i * 2 + 1]) << 8));
                }
                soundTouch->putSamples(sampleBuffer, nb);
                num = soundTouch->receiveSamples(sampleBuffer, data_size / 4);
            } else {
                soundTouch->flush();
            }
        }
        if (num == 0) {
            finished = true;
            continue;
        } else {
            if (out_buffer == NULL) {
                num = soundTouch->receiveSamples(sampleBuffer, data_size / 4);
                if (num == 0) {
                    finished = true;
                    continue;
                }
            }
            return num;
        }
    }
    return 0;
}

/**
 * 音频数据回调
 * **/
void pcmBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void *context) {
    OpenSLESPlay *play = (OpenSLESPlay *) context;
    if (play != NULL) {
        //获取采样个数
//        int bufferSize = play->resampleAudio();
        int bufferSize = play->getSoundTouchData();
        if (bufferSize > 0) {
            play->clock += bufferSize / ((double) (play->sample_rate * 2 * 2));
            //每隔0.1秒回调一次当前时间进度
            if (abs(play->clock - play->last_time) >= 0.1) {
                play->last_time = play->clock;
                //播放进度回调应用层
                play->callJava->onCallTime(CHILD_THREAD, play->clock, play->duration);
            }
            //声音振幅回调
            play->callJava->onCallVolumeDB(CHILD_THREAD,
                                           play->getPcmDB(
                                                   reinterpret_cast<char *>(play->sampleBuffer),
                                                   bufferSize * 4));
            //将音频数据输入到播放器处理队列中
            (*play->pcmBufferQueue)->Enqueue(play->pcmBufferQueue, (char *) play->sampleBuffer,
                                             bufferSize * 2 * 2);
//            (*play->pcmBufferQueue)->Enqueue(play->pcmBufferQueue, (char *) play->buffer,
//                                             bufferSize);
        } else {
            play->clock = 0;
            play->last_time = 0;
        }
    }
}

/**
 * 初始化OpenSLES
 * **/
void OpenSLESPlay::initOpenSLES() {

    SLresult result;
    result = slCreateEngine(&engineObject, 0, 0, 0, 0, 0);
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

    //第二步，创建混音器
    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, mids, mreq);
    (void) result;
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    (void) result;
    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
                                              &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) {
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings);
        (void) result;
    }
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&outputMix, 0};


    // 第三步，配置PCM格式信息
    SLDataLocator_AndroidSimpleBufferQueue android_queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                            2};

    SLDataFormat_PCM pcm = {
            SL_DATAFORMAT_PCM,//播放pcm格式的数据
            2,//2个声道（立体声）
            static_cast<SLuint32>(getCurrentSampleRateForOpenSles(sample_rate)),//44100hz的频率
            SL_PCMSAMPLEFORMAT_FIXED_16,//位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,//和位数一致就行
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,//立体声（前左前右）
            SL_BYTEORDER_LITTLEENDIAN//结束标志
    };
    SLDataSource slDataSource = {&android_queue, &pcm};


    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_MUTESOLO};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    (*engineEngine)->CreateAudioPlayer(engineEngine, &pcmPlayerObject, &slDataSource, &audioSnk, 3,
                                       ids, req);
    //初始化播放器
    (*pcmPlayerObject)->Realize(pcmPlayerObject, SL_BOOLEAN_FALSE);

    //获取Player接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_PLAY, &pcmPlayerPlay);

    //获取音量接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_VOLUME, &pcmVolumePlay);

    //获取声道接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_MUTESOLO, &pcmMutePlay);

    //注册回调缓冲区 获取缓冲队列接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_BUFFERQUEUE, &pcmBufferQueue);

    //设置音量
    setVolume(volumePercent);
    //设置声道
    setMute(mute);
    //缓冲接口回调,当播放器完成对前一个缓冲区队列的播放时，回调函数会被调用，然后我们又继续读取音频数据，直到结束
    (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue, pcmBufferCallBack, this);
    //获取播放状态接口
    (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    //开始，让第一个缓冲区入队
    pcmBufferCallBack(pcmBufferQueue, this);


}

/**
 * 采样率转换成OpenSLES对应的采样率标识
 * sample_rate: 采样率
 * **/
int OpenSLESPlay::getCurrentSampleRateForOpenSles(int sample_rate) {
    int rate = 0;
    switch (sample_rate) {
        case 8000:
            rate = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            rate = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            rate = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            rate = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            rate = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            rate = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            rate = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            rate = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            rate = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            rate = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            rate = SL_SAMPLINGRATE_88_2;
            break;
        case 96000:
            rate = SL_SAMPLINGRATE_96;
            break;
        case 192000:
            rate = SL_SAMPLINGRATE_192;
            break;
        default:
            rate = SL_SAMPLINGRATE_44_1;
    }
    return rate;
}

/**
 * 暂停音频播放
 * **/
void OpenSLESPlay::pause() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PAUSED);
    }
}

/**
 * 恢复音频播放
 * **/
void OpenSLESPlay::resume() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    }
}

/**
 * 停止音频播放
 * **/
void OpenSLESPlay::stop() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_STOPPED);
    }
}

/**
 * 释放资源
 * **/
void OpenSLESPlay::release() {

    if (queue != NULL) {
        delete (queue);
        queue = NULL;
    }

    if (pcmPlayerObject != NULL) {
        (*pcmPlayerObject)->Destroy(pcmPlayerObject);
        pcmPlayerObject = NULL;
        pcmPlayerPlay = NULL;
        pcmBufferQueue = NULL;
    }

    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentalReverb = NULL;
    }

    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

    if (buffer != NULL) {
        free(buffer);
        buffer = NULL;
    }

    if (avCodecContext != NULL) {
        avcodec_close(avCodecContext);
        avcodec_free_context(&avCodecContext);
        avCodecContext = NULL;
    }

    if (playState != NULL) {
        playState = NULL;
    }
    if (callJava != NULL) {
        callJava = NULL;
    }
    if (soundTouch != NULL) {
        soundTouch = NULL;
    }
    if (sampleBuffer != NULL) {
        sampleBuffer = NULL;
    }
    if (out_buffer != NULL) {
        out_buffer = NULL;
    }

}

/**
 * 设置音量
 * SetVolumeLevel范围为0 - -5000，0为最大，-5000为最小
 * **/
void OpenSLESPlay::setVolume(int percent) {
    volumePercent = percent;
    //确保声音线性调整
    if (pcmVolumePlay != NULL) {
        if (percent > 30) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -20);
        } else if (percent > 25) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -22);
        } else if (percent > 20) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -25);
        } else if (percent > 15) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -28);
        } else if (percent > 10) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -30);
        } else if (percent > 5) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -34);
        } else if (percent > 3) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -37);
        } else if (percent > 0) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -40);
        } else {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (100 - percent) * -100);
        }
    }
}

/**
 * 设置播放声道
 * **/
void OpenSLESPlay::setMute(int mute) {
    this->mute = mute;
    if (pcmMutePlay != NULL) {
        if (mute == 0) {
            //右声道
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, true);
        } else if (mute == 1) {
            //左声道
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, true);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, false);
        } else if (mute == 2) {
            //立体声
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, false);
        }

    }
}

/**
 * 设置播放音调，正常为1.0f
 * **/
void OpenSLESPlay::setPitch(float pitch) {
    this->pitch = pitch;
    if (soundTouch != NULL) {
        soundTouch->setPitch(pitch);
    }
}

/**
 * 设置播放速度，正常为1.0f
 * **/
void OpenSLESPlay::setSpeed(float speed) {
    this->speed = speed;
    if (soundTouch != NULL) {
        soundTouch->setTempo(speed);
    }
}

/**
 * 获取声音振幅
 * **/
int OpenSLESPlay::getPcmDB(char *pcmData, size_t pcmSize) {
    int db = 0;
    short int pervalue = 0;
    double sum = 0;
    for (int i = 0; i < pcmSize; i += 2) {
        memcpy(&pervalue, pcmData + i, 2);
        sum += abs(pervalue);
    }
    sum = sum / (pcmSize / 2);
    if (sum > 0) {
        db = (int) 20.0 * log10(sum);
    }
    return db;
}

OpenSLESPlay::~OpenSLESPlay() {

}
