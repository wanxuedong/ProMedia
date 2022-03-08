#include "WlAudio.h"

WlAudio::WlAudio(WlPlaystatus *playstatus, int sample_rate, WlCallJava *callJava) {
    this->callJava = callJava;
    this->playstatus = playstatus;
    this->sample_rate = sample_rate;
    queue = new WlQueue(playstatus);
    buffer = (uint8_t *) av_malloc(sample_rate * 2 * 2);

    pthread_mutex_init(&codecMutex, NULL);

    //存储的每一帧的数据大小为采样位数 * 采样通道 *
    sampleBuffer = static_cast<SAMPLETYPE *>(malloc(sample_rate * 2 * 2));
    soundTouch = new SoundTouch();
    soundTouch->setSampleRate(sample_rate);
    soundTouch->setChannels(2);
    soundTouch->setPitch(pitch);
    soundTouch->setTempo(speed);
}

WlAudio::~WlAudio() {
    pthread_mutex_destroy(&codecMutex);
}

void *decodPlay(void *data) {
    WlAudio *wlAudio = (WlAudio *) data;

    wlAudio->initOpenSLES();

    return 0;
}

void WlAudio::play() {

    if (playstatus != NULL && !playstatus->exit) {
        pthread_create(&thread_play, NULL, decodPlay, this);
    }

}

/**
 * 从队列中取出音频的数据
 * **/
int WlAudio::resampleAudio(void **pcmbuf) {
    data_size = 0;
    while (playstatus != NULL && !playstatus->exit) {

        if (playstatus->seek) {
            av_usleep(INETRVAL_TIME);
            continue;
        }

        if (queue->getQueueSize() == 0)//加载中
        {
            av_usleep(INETRVAL_TIME);
            if (!playstatus->load) {
                playstatus->load = true;
                callJava->onCallLoad(CHILD_THREAD, true);
            }
            av_usleep(INETRVAL_TIME);
            continue;
        } else {
            if (playstatus->load) {
                playstatus->load = false;
                callJava->onCallLoad(CHILD_THREAD, false);
            }
        }
        avPacket = av_packet_alloc();
        //queue->getAvPacket可能会休眠，所以无需再次调用av_usleep(INETRVAL_TIME)降低速率
        if (queue->getAvPacket(avPacket) != 0) {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            continue;
        }
        pthread_mutex_lock(&codecMutex);
        //发送avPacket数据到ffmepg，放到解码队列中
        ret = avcodec_send_packet(avCodecContext, avPacket);
        if (ret != 0) {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            pthread_mutex_unlock(&codecMutex);
            continue;
        }
        avFrame = av_frame_alloc();
        //将成功的解码队列中取出1个frame
        ret = avcodec_receive_frame(avCodecContext, avFrame);
        if (ret == 0) {

            //为了防止出现意外情况，保持通道数和通道布局保持一致
            if (avFrame->channels && avFrame->channel_layout == 0) {
                avFrame->channel_layout = av_get_default_channel_layout(avFrame->channels);
            } else if (avFrame->channels == 0 && avFrame->channel_layout > 0) {
                avFrame->channels = av_get_channel_layout_nb_channels(avFrame->channel_layout);
            }

            //初始化重采样配置
            SwrContext *swr_ctx;
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
                pthread_mutex_unlock(&codecMutex);
                continue;
            }

            //重采样获取采样个数
            nb = swr_convert(
                    swr_ctx,
                    &buffer,
                    avFrame->nb_samples,
                    (const uint8_t **) avFrame->data,
                    avFrame->nb_samples);

            //根据通道布局(ffmpeg中表示通道个数的封装概念)获取通道数
            int out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
            //计算数据大小   =   采样个数 * 通道数 * 采样位数
            data_size = nb * out_channels * av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);

            //从帧中获取当前的时间并乘以time_base，即可获取当前的播放时间
            now_time = avFrame->pts * av_q2d(time_base);
            if (now_time < clock) {
                now_time = clock;
            }
            clock = now_time;
            if (pcmbuf != NULL) {
                *pcmbuf = buffer;
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            pthread_mutex_unlock(&codecMutex);
            swr_free(&swr_ctx);
            break;
        } else {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            pthread_mutex_unlock(&codecMutex);
            continue;
        }
    }
    return data_size;
}

/**
 * 需要注意的是使用soundTouch处理后的声音会间隔出现噪点,正常播放视频最好不要开启
 * **/
void pcmBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void *context) {
    WlAudio *wlAudio = (WlAudio *) context;
    if (wlAudio != NULL) {
        //需要注意的是这里使用的是利用soundTouch处理过的数据，
//        int bufferSize = wlAudio->getSoundTouchData();
//        if (bufferSize > 0) {
//            //起始获取的播放时间不断加上准备播放的时间，等于当前的播放时间
//            wlAudio->clock += bufferSize / ((double) (wlAudio->sample_rate * 2 * 2));
//            if (wlAudio->clock - wlAudio->last_time >= 1) {
//                wlAudio->last_time = wlAudio->clock;
//                //回调应用层
//                wlAudio->callJava->onCallTimeInfo(CHILD_THREAD, wlAudio->clock, wlAudio->duration);
//            }
//            wlAudio->callJava->onCallVolumeDB(CHILD_THREAD, wlAudio->getPcmDb(
//                    reinterpret_cast<char *>(wlAudio->sampleBuffer), bufferSize * 4));
//            //开始执行播放逻辑，传入数据和数据大小
//            (*wlAudio->pcmBufferQueue)->Enqueue(wlAudio->pcmBufferQueue,
//                                                (char *) wlAudio->sampleBuffer,
//                                                bufferSize * 2 * 2);
//        }
        //这里使用的是正常的音频的数据
        int bufferSize = wlAudio->resampleAudio(NULL);
        if (bufferSize > 0) {
            //起始获取的播放时间不断加上准备播放的时间，等于当前的播放时间
            wlAudio->clock += bufferSize / ((double) (wlAudio->sample_rate * 2 * 2));
            if (wlAudio->clock - wlAudio->last_time >= 1) {
                wlAudio->last_time = wlAudio->clock;
                //回调应用层
                wlAudio->callJava->onCallTimeInfo(CHILD_THREAD, wlAudio->clock, wlAudio->duration);
            }
            wlAudio->callJava->onCallVolumeDB(CHILD_THREAD, wlAudio->getPcmDb(
                    reinterpret_cast<char *>(wlAudio->buffer), bufferSize * 4));
            //开始执行播放逻辑，传入数据和数据大小
            (*wlAudio->pcmBufferQueue)->Enqueue(wlAudio->pcmBufferQueue, (char *) wlAudio->buffer,
                                                bufferSize);
        } else{
            wlAudio->clock = 0;
        }
    }
}

void WlAudio::initOpenSLES() {

    SLresult result;
    slCreateEngine(&engineObject, 0, 0, 0, 0, 0);
    (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

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
            static_cast<SLuint32>(getCurrentSampleRateForOpensles(sample_rate)),//44100hz的频率
            SL_PCMSAMPLEFORMAT_FIXED_16,//位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,//和位数一致就行
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,//立体声（前左前右）
            SL_BYTEORDER_LITTLEENDIAN//结束标志
    };
    SLDataSource slDataSource = {&android_queue, &pcm};

    //里面的参数SL_IID_PLAYBACKRATE作用是自动识别调整采样率，避免播放卡顿的问题
    const SLInterfaceID ids[4] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_PLAYBACKRATE,
                                  SL_IID_MUTESOLO};
    const SLboolean req[4] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    (*engineEngine)->CreateAudioPlayer(engineEngine, &pcmPlayerObject, &slDataSource, &audioSnk, 4,
                                       ids, req);
    //初始化播放器
    (*pcmPlayerObject)->Realize(pcmPlayerObject, SL_BOOLEAN_FALSE);

    //得到接口后调用  获取Player接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_PLAY, &pcmPlayerPlay);
    //得到接口后调用  获取声音控制接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_VOLUME, &pcmVolumePlay);
    //获取声道接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_MUTESOLO, &pcmMutePlay);

    //注册回调缓冲区 获取缓冲队列接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_BUFFERQUEUE, &pcmBufferQueue);
    //设置默认音量
    setVolume(volumePercent);
    //缓冲接口回调
    (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue, pcmBufferCallBack, this);
    //获取播放状态接口
    (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    pcmBufferCallBack(pcmBufferQueue, this);

}

/**
 * 将具体的采样率转化成SLES中的标示
 * **/
int WlAudio::getCurrentSampleRateForOpensles(int sample_rate) {
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

void WlAudio::pause() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PAUSED);
    }
}

void WlAudio::resume() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    }
}

void WlAudio::release() {

    if (queue != NULL) {
        queue->noticeQueue();
    }
    pthread_join(thread_play, NULL);

    if (queue != NULL) {
        delete (queue);
        queue = NULL;
    }

    if (pcmPlayerObject != NULL) {
        (*pcmPlayerObject)->Destroy(pcmPlayerObject);
        pcmPlayerObject = NULL;
        pcmPlayerPlay = NULL;
        pcmBufferQueue = NULL;
        pcmVolumePlay = NULL;
        pcmMutePlay = NULL;
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

    if (out_buffer != NULL) {
        out_buffer = NULL;
    }

    if (soundTouch != NULL) {
        delete soundTouch;
        soundTouch = NULL;
    }

    if (sampleBuffer != NULL) {
        free(sampleBuffer);
        sampleBuffer = NULL;
    }

    if (avCodecContext != NULL) {
        avcodec_close(avCodecContext);
        avcodec_free_context(&avCodecContext);
        avCodecContext = NULL;
    }

    if (playstatus != NULL) {
        playstatus = NULL;
    }
    if (callJava != NULL) {
        callJava = NULL;
    }

}

void WlAudio::stop() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_STOPPED);
    }
}

/**
 * 为了保证声音能够均匀的控制放大见效，需要进行分段处理
 * **/
void WlAudio::setVolume(int percent) {
    if (percent < 0) {
        volumePercent = 0;
    } else if (percent > 100) {
        volumePercent = 100;
    } else {
        volumePercent = percent;
    }
    /**
     * pcmVolumePlay中的音量最大为0，最小为-5000，这里做分段处理
     * **/
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
 * 设置播放声道模式
 * **/
void WlAudio::setMute(int mute) {
    this->mute = mute;
    if (pcmMutePlay != NULL) {
        if (mute == 0)//right
        {
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, true);
        } else if (mute == 1)//left
        {
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, true);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, false);
        } else if (mute == 2)//center
        {
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, false);
        }
    }
}

/**
 * 获取经过soundTouch处理后的数据，并返回数据大小
 * **/
int WlAudio::getSoundTouchData() {

    while (playstatus != NULL && !playstatus->exit) {
        out_buffer = NULL;
        if (finished) {
            finished = false;
            data_size = resampleAudio(reinterpret_cast<void **>(&out_buffer));
            if (data_size > 0) {
                //需要注意的是FFmpeg解码后的数据采样位数是8bit，soundTouch中最低采样位数是16bit，所以需要进行转换
                for (int i = 0; i < data_size / 2 + 1; i++) {
                    sampleBuffer[i] = (out_buffer[i * 2] | ((out_buffer[i * 2 + 1]) << 8));
                }
                if (sampleBuffer != NULL && nb > 0) {
                    //添加数据到soundTouch
                    soundTouch->putSamples(sampleBuffer, nb);
                    //获取处理后的数据大小，这里4为 = 通道数(2) * 采样位数(16bit)，因为data_size的大小是经过处理的
                    num = soundTouch->receiveSamples(sampleBuffer, data_size / 4);
                } else {
                    num = 0;
                }

            } else {
                soundTouch->flush();
            }
        }
        if (num == 0) {
            finished = true;
            continue;
        } else {
            if (out_buffer == NULL) {
                //获取处理后的数据大小，这里4为 = 通道数(2) * 采样位数(16bit)
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
 * 设置音调，正常为1
 * **/
void WlAudio::setPitch(float pitch) {
    this->pitch = pitch;
    if (soundTouch != NULL) {
        soundTouch->setPitch(pitch);
    }
}

/**
 * 设置播放速度，正常为1
 * **/
void WlAudio::setSpeed(float speed) {
    this->speed = speed;
    if (soundTouch != NULL) {
        soundTouch->setTempo(speed);
    }
}

/**
 * 通过解析每一帧的采样位数，取其平均值，再通过公式，获取即时的播放声音大小
 * **/
int WlAudio::getPcmDb(char *pcmcata, size_t pcmsize) {
    int db = 0;
    short int pervalue = 0;
    double sum = 0;
    for (int i = 0; i < pcmsize; i += 2) {
        memcpy(&pervalue, pcmcata + i, 2);
        sum += abs(pervalue);
    }
    sum = sum / (pcmsize / 2);
    if (sum > 0) {
        db = (int) 20.0 * log10(sum);
    }
    return db;
}