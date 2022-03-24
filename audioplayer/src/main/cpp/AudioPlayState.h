//
// Created by mac on 2021/12/10.
// 音频播放状态
//

#ifndef TEST_AUDIOPLAYSTATE_H
#define TEST_AUDIOPLAYSTATE_H


class AudioPlayState {

public:
    //是否退出
    bool exit = false;
    //是否处于首次加载状态
    bool load = true;
    //是否首次加载完毕
    bool loadOver = false;
    //是否处于播放过程中的加载状态
    bool loading = true;
    //是否处于调节进度状态
    bool seek = false;
public:
    AudioPlayState();

    ~AudioPlayState();

};


#endif //TEST_AUDIOPLAYSTATE_H
