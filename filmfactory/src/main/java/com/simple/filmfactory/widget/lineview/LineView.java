package com.simple.filmfactory.widget.lineview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * 导航栏中间绘制的线条
 *
 * @author simpo**/
public class LineView extends View {

    private Paint paint;
    private int width;

    public LineView(Context context, int width, int height) {
        super(context);
        init(context, width, height);
    }

    public LineView(Context context, AttributeSet attrs, int height) {
        super(context, attrs);
        init(context, width, height);
    }

    public LineView(Context context, AttributeSet attrs, int defStyleAttr, int height) {
        super(context, attrs, defStyleAttr);
        init(context, width, height);
    }

    private void init(Context context, int width, int height) {
        this.width = width;
        paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(ScreenUtil.dip2px(context, height));
    }

    //设置线条颜色
    public void setLineColor(int color) {
        paint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, width, 0, paint);
    }

}
