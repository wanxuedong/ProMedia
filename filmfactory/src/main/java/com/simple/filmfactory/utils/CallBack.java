package com.simple.filmfactory.utils;

/**
 * 用作数据回调使用
 *
 * @author simpo*/


public interface CallBack<T> {

    T call(String... data);

}
