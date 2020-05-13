package com.simpo.promusic.player.threadXUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wanxuedong
 * 添加在主线程的回调操作
 **/
public class AbstractLife implements Runnable {

    /**
     * 线程名称,可传，可不传
     **/
    private String name = "";
    /**
     * 用于替换当前的对象，当使用ThreadXFactory的第一个构造器时可用上
     * 父类的run和target只能有一个执行，有target，就执行target的run
     **/
    private Runnable target;

    public String getName() {
        name = deCodeName() + name;
        return name;
    }

    public AbstractLife() {

    }

    public AbstractLife(Runnable runnable) {
        target = runnable;
    }

    public AbstractLife(String name) {
        this.name = name;
    }

    /**
     * 处理耗时事件，在子线程
     **/
    @Override
    public void run() {

    }

    /**
     * 耗时事件结束后调用，在UI线程
     **/
    public void end() {
    }

    public Runnable getTarget() {
        return target;
    }

    /**
     * 获取线程名称
     **/
    private String deCodeName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

}
