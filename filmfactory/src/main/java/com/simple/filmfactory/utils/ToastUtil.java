package com.simple.filmfactory.utils;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * 封装Toast
 * 实现功能
 * 1：展示文字判空处理
 * 2：避免重复调用和在子线程中调用
 * 3：避免开启应用就实现初始化降低开启速率
 **/
public class ToastUtil extends BaseUtil {

    //上次点击时间
    private static long lastTime;
    //点击处理间隔时长
    private static final int INTERVAlTIME = 2000;
    private static Handler handler;

    /***
     * 工具类初始化方法，但不需要主动调用，在Application设置监听回调即可
     * **/
    private static void init() {
        if (handler != null) {
            return;
        }
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                show(msg.obj.toString());
            }
        };
    }

    public static void show(Object object) {
        show(object + "");
    }

    /**
     * 检查是否已经初始化，需要在Application中设置监听实现，
     * 这样做的好处是不会在应用一开始实现初始化，提高应用开启速度
     */
    public static void show(String content) {
        synchronized (ToastUtil.class) {
            checkInit();
            init();
            if (TextUtils.isEmpty(content)) {
                return;
            }
            if (System.currentTimeMillis() - lastTime > INTERVAlTIME) {
                lastTime = System.currentTimeMillis();
                /*
                 * 判断是否在主线程调用，如果不是，切换到主线程
                 **/
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
                } else {
                    lastTime = 0;
                    Message message = handler.obtainMessage();
                    message.obj = content;
                    handler.sendMessage(message);
                }
            }
        }
    }

}
