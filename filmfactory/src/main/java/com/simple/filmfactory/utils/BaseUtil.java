package com.simple.filmfactory.utils;

import android.app.Application;

/**
 * 通用工具类基类
 * 防止应用开启工具就初始化，减小对应用启动时间的影响
 * 继承的时候只需要重写initMethod方法，返回当前类名即可
 * 提供了Context，但是使用前必须先调用checkInit方法
 **/

public abstract class BaseUtil {

    //初始化工具类实现回调
    private static CallBack<Application> callBack;
    //工具类是否完成了初始化
    private static Boolean hasInit = false;
    //想用使用context，必须要先调用checkInit
    public static Application context;

    protected BaseUtil() {
    }

    /**
     * 初始化工具类
     **/
    private static void baseInit(Application app) {
        context = app;
    }

    /**
     * 会先检查是否已经完成了初始化，未初始化会重新初始化
     * 未设置初始化监听器会抛出异常
     **/
    protected static void checkInit() {
        if (!hasInit) {
            if (callBack != null) {
                baseInit(callBack.call());
            } else {
                throw new NullPointerException("please use setInitCall Method for init Utils");
            }
        }
    }

    /**
     * 设置初始化对调监听
     * **/
    public static void setCallBack(CallBack<Application> call){
        callBack = call;
    }


}
