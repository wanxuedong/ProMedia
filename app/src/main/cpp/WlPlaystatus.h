//
// Created by simpo on 2020/4/15.
// 用于表示当前播放是否结束
//

#ifndef PROMUSIC_WLPLAYSTATUS_H
#define PROMUSIC_WLPLAYSTATUS_H


class WlPlaystatus {

public:
    bool exit = false;
    bool load = true;
    bool seek = false;
    bool pause = false;

public:
    WlPlaystatus();
    ~WlPlaystatus();

};


#endif
