//
// Created by yangw on 2018-3-6.
//

#include "AudioQueue.h"

AudioQueue::AudioQueue(AudioPlayState *playState) {
    this->playState = playState;
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);

}

/**
 * 音频入队
 * **/
int AudioQueue::putAvPacket(AVPacket *packet) {

    pthread_mutex_lock(&mutexPacket);

    queuePacket.push(packet);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

    return 0;
}

/**
 * 音频出队
 * **/
int AudioQueue::getAvPacket(AVPacket *packet) {

    pthread_mutex_lock(&mutexPacket);

    while (playState != NULL && !playState->exit) {
        if (queuePacket.size() > 0) {
            AVPacket *avPacket = queuePacket.front();
            if (av_packet_ref(packet, avPacket) == 0) {
                queuePacket.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            break;
        } else {
            pthread_cond_wait(&condPacket, &mutexPacket);
        }
    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

/**
 * 获取队列大小
 * **/
int AudioQueue::getQueueSize() {

    int size = 0;
    pthread_mutex_lock(&mutexPacket);
    size = queuePacket.size();
    pthread_mutex_unlock(&mutexPacket);

    return size;
}

/**
 * 清除队列数据
 * **/
void AudioQueue::clearAvPacket() {
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

    while (!queuePacket.empty()) {
        AVPacket *packet = queuePacket.front();
        queuePacket.pop();
        av_packet_free(&packet);
        av_free(packet);
        packet = NULL;
    }
    pthread_mutex_unlock(&mutexPacket);

}

/**
 * 队列是否达到最大存储量
 * **/
bool AudioQueue::isFull() {
    if (getQueueSize() > MAX_SIZE) {
        return true;
    }
    return false;
}

AudioQueue::~AudioQueue() {
    clearAvPacket();
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);
}
