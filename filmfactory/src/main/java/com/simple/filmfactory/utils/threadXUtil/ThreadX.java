package com.simple.filmfactory.utils.threadXUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wanxuedong
 * 维护线程操作工具类
 * 维护了一个线程池，并且统计了维护的线程数量，添加end方法执行在主线程
 **/
public class ThreadX {

    /**
     * 工具类单例
     **/
    private static ThreadX threadX;

    /**
     * 最小维持的核心线程数量
     **/
    private final int CORE_POOL_SIZE = 4;

    /**
     * 维护任务的线程池
     */
    private ThreadPoolExecutor executor;
    private ThreadXFactory threadFactory;

    /**
     * 创建线程池
     **/
    private ThreadX() {
        threadFactory = new ThreadXFactory();
        //创建一个最小维持12个核心线程，无上限大小限制，最长保活60s的线程池,注意不要加ThreadFactory参数，逻辑会冲突
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 创建单例
     **/
    public static ThreadX x() {
        if (threadX == null) {
            synchronized (ThreadX.class) {
                if (threadX == null) {
                    threadX = new ThreadX();
                }
            }
        }
        return threadX;
    }

    /**
     * 添加并执行一个新线程
     **/
    public void run(AbstractLife life) {
        if (life != null) {
            executor.execute(threadFactory.newThread(life));
        }
    }

    public ThreadXFactory getThreadFactory() {
        return threadFactory;
    }
}
