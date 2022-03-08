package com.simple.filmfactory.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 用于录像计数的视图
 *
 * @author wanxuedong
 */
public class TimeClock extends View {

    private Paint textPaint;
    private Paint rolePaint;
    private int currentTime = 0;
    private Handler handler;

    private boolean start;

    /**
     * 控件宽度
     **/
    private int width;

    /**
     * 字体大小
     **/
    private int textSize = 120;
    /**
     * 红点半径
     **/
    private int radius = 30;

    private final int REFRESH = 10000;

    public TimeClock(Context context) {
        super(context);
        init();
    }

    public TimeClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeClock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new Paint();
        rolePaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        rolePaint.setAntiAlias(true);
        rolePaint.setColor(Color.RED);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (start && msg.what == REFRESH) {
                    send();
                    invalidate();
                }
            }
        };
    }

    /**
     * 发送定时消息
     * **/
    private void send() {
        handler.removeMessages(REFRESH);
        Message message = new Message();
        message.what = REFRESH;
        handler.sendMessageDelayed(message, 1000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String time = timeFormat(currentTime++);
        if (currentTime % 2 == 0) {
            canvas.drawCircle(width / 2 - textSize * time.length() / 3, (float) (textSize - radius * 1.5), radius, rolePaint);
        }
        canvas.drawText(time, width / 2 - textSize, textSize, textPaint);
    }

    /**
     * 时间格式化成我们需要的字符串
     **/
    private String timeFormat(int time) {
        if (time <= 0) {
            return "00:00";
        }
        String hour;
        String minute;
        String second;
        hour = time / 60 / 60 + "";
        minute = (time / 60) % 60 + "";
        second = time % 60 + "";
        if (hour == null || "0".equals(hour)) {
            hour = "00";
        }
        if (minute == null || "0".equals(minute)) {
            minute = "00";
        }
        if (second == null || "0".equals(second)) {
            second = "00";
        }
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        if (second.length() == 1) {
            second = "0" + second;
        }
        if (hour == null || "00".equals(hour)) {
            return minute + ":" + second;
        }
        return hour + ":" + minute + ":" + second;
    }

    /**
     * 开始计时
     **/
    public void start() {
        currentTime = 0;
        start = true;
        send();
    }

    /**
     * 停止计时
     **/
    public void stop() {
        start = false;
    }

}
