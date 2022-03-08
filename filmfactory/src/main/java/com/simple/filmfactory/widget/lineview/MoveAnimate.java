package com.simple.filmfactory.widget.lineview;

import android.animation.ValueAnimator;
import android.view.View;

/**
 * 执行属性动画的工具类
 * **/

public class MoveAnimate {

    //用于执行动画的工具
    public static void marginValueAnimator(View view, float from, float to, long time, boolean repeat, ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(from, to);
        mAnimator.addUpdateListener(updateListener);
        //4.设置动画的持续时间、是否重复及重复次数等属性
        mAnimator.setDuration(time);
        if (repeat) {
            mAnimator.setRepeatCount(-1);
            mAnimator.setRepeatMode(ValueAnimator.RESTART);
        }
        mAnimator.setTarget(view);
        mAnimator.start();
    }

    //用于执行动画的工具
    public static void marginValueAnimator(View view, float from, float to, float end, long time, boolean repeat, ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(from, to, end);
        mAnimator.addUpdateListener(updateListener);
        //4.设置动画的持续时间、是否重复及重复次数等属性
        mAnimator.setDuration(time);
        if (repeat) {
            mAnimator.setRepeatCount(-1);
            mAnimator.setRepeatMode(ValueAnimator.RESTART);
        }
        mAnimator.setTarget(view);
        mAnimator.start();
    }

}
