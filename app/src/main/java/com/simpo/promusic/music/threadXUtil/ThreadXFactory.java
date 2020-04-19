package com.simpo.promusic.music.threadXUtil;

import android.util.Log;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wanxuedong
 * 创建线程工厂，主要用于产生和统计线程数量
 **/
public class ThreadXFactory implements ThreadFactory {

    private final String TAG = "ThreadX_Info";

    /**
     * 当前维护的线程数量
     **/
    private AtomicInteger atomicInteger = new AtomicInteger();

    /**
     * 创建一个线程
     **/
    @Override
    public Thread newThread(Runnable r) {
        AbstractLife life = new AbstractLife(r);
        ThreadXSingle single = new ThreadXSingle(atomicInteger, life);
        atomicInteger.incrementAndGet();
        Log.d(TAG, "统计线程数量:" + atomicInteger.get() + " 加入线程: " + single.getName());
        return single;
    }

    /**
     * 创建一个线程
     **/
    public Thread newThread(AbstractLife life) {
        ThreadXSingle single = new ThreadXSingle(atomicInteger, life);
        atomicInteger.incrementAndGet();
        Log.d(TAG, "统计线程数量:" + atomicInteger.get() + " 加入线程: " + single.getName());
        return single;
    }

}
