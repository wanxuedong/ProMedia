package com.simpo.promusic.player.threadXUtil;


import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wanxuedong
 * 真正实现功能的线程
 **/
class ThreadXSingle extends Thread {

    private final String TAG = "ThreadX_Info";
    private AtomicInteger atomicInteger;
    private AbstractLife life;

    public ThreadXSingle(AtomicInteger atomicInteger, AbstractLife life) {
        super(life, life.getName());
        this.life = life;
        this.atomicInteger = atomicInteger;
    }

    @Override
    public final void run() {
        Log.d(TAG, "统计线程数量:" + atomicInteger.get() + " 开始线程: " + getName());
        if (life.getTarget() != null) {
            life.getTarget().run();
        } else {
            life.run();
        }
        atomicInteger.getAndDecrement();
        Log.d(TAG, "统计线程数量:" + atomicInteger.get() + " 结束线程: " + getName());
    }


}
