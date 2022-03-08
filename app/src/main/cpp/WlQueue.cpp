#include "WlQueue.h"

WlQueue::WlQueue(WlPlaystatus *wlPlaystatus) {
    this->wlPlaystatus = wlPlaystatus;
    pthread_mutex_init(&pthreadMutex, NULL);
    //初始化条件变量
    pthread_cond_init(&pthreadCond, NULL);
}

WlQueue::~WlQueue() {

    clearAvPacket();
    pthread_mutex_destroy(&pthreadMutex);
    pthread_cond_destroy(&pthreadCond);

}

int WlQueue::putAvPacket(AVPacket *avPacket) {
    //互斥锁上锁
    pthread_mutex_lock(&pthreadMutex);
    queuePacket.push(avPacket);
    //激活等待列表中的线程
    pthread_cond_signal(&pthreadCond);
    //互斥锁解锁
    pthread_mutex_unlock(&pthreadMutex);
    return 0;
}

/**
 * 获取packet，关于里面的指针和内存的释放需要深刻理解
 * **/
int WlQueue::getAvPacket(AVPacket *avPacket) {
    //互斥锁上锁
    pthread_mutex_lock(&pthreadMutex);
    while (wlPlaystatus != NULL && !wlPlaystatus->exit) {
        if (queuePacket.size() > 0) {
            //返回第一个数据的读/写引用
            AVPacket *packet = queuePacket.front();
            //复制packet数据到avPacket中，并给packet添加avPacket的计数
            if (av_packet_ref(avPacket, packet) == 0) {
                //数据成功赋值出去，就可以从队列中弹出数据
                queuePacket.pop();
            }
            //释放packet指针
            av_packet_free(&packet);
            //释放packet分配的内存
            av_free(packet);
            //赋值为NULL
            packet = NULL;
            break;
        } else {
            //等待条件变量
            pthread_cond_wait(&pthreadCond, &pthreadMutex);
        }
    }
    //互斥锁解锁
    pthread_mutex_unlock(&pthreadMutex);
    return 0;
}

void WlQueue::clearAvPacket() {
    pthread_cond_signal(&pthreadCond);
    pthread_mutex_lock(&pthreadMutex);

    while (!queuePacket.empty()) {
        AVPacket *packet = queuePacket.front();
        queuePacket.pop();
        av_packet_free(&packet);
        av_free(packet);
        packet = NULL;
    }
    pthread_mutex_unlock(&pthreadMutex);

}

int WlQueue::getQueueSize() {
    int size = 0;
    //互斥锁上锁
    pthread_mutex_lock(&pthreadMutex);
    size = queuePacket.size();
    //互斥锁解锁
    pthread_mutex_unlock(&pthreadMutex);
    return size;
}

void WlQueue::noticeQueue() {
    pthread_cond_signal(&pthreadCond);
}
