package com.simple.filmfactory.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.Gravity;

/**
 * 添加水印工具
 **/
public class WaterUtil {

    /**
     * 给Bitmap添加文字或图片水印，只支持左上，左下，右上，右下四个位置，不支持文字换行
     **/
    public static Bitmap addWater(Context context, Bitmap src, Bitmap watermark,
                                  String title, int gravity) {
        int margin = DimensionUtil.dip2px(context, 15);
        int textHeight = DimensionUtil.dip2px(context, 18);
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        //需要处理图片太大造成的内存超过的问题,这里我的图片很小所以不写相应代码了
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        // 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
        Paint paint = new Paint();
        //加入图片
        if (watermark != null) {
            int ww = watermark.getWidth();
            int wh = watermark.getHeight();
            paint.setAlpha(50);
            cv.drawBitmap(watermark, w - ww + 5, h - wh + 5, paint);// 在src的右下角画入水印
        }
        //加入文字
        if (title != null) {
            Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/华文新魏.ttf");
            TextPaint textPaint = new TextPaint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(font);
            textPaint.setAlpha(50);
            textPaint.setTextSize(textHeight);
            int left = 0;
            int top = 0;
            Rect rect = new Rect();
            textPaint.getTextBounds(title, 0, title.length(), rect);
            cv.drawRect(rect, textPaint);
            switch (gravity) {
                case Gravity.LEFT:
                    top = margin + textHeight;
                    left = margin;
                    break;
                case Gravity.LEFT | Gravity.BOTTOM:
                    top = h - margin;
                    left = margin;
                    break;
                case Gravity.RIGHT:
                    top = margin + textHeight;
                    left = w - rect.width() - margin;
                    break;
                case Gravity.RIGHT | Gravity.BOTTOM:
                    top = h - margin;
                    left = w - rect.width() - margin;
                    break;
                default:
            }
            cv.drawText(title, left, top, textPaint);
        }
        cv.save();// 保存
        cv.restore();// 存储
        return newb;
    }

}
