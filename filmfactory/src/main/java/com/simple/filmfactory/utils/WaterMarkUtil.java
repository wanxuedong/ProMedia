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
 * @author wan
 * 创建日期：2022/08/04
 * 描述：水印处理工具类
 **/
public class WaterMarkUtil {

    /**
     * 给Bitmap添加文字或图片静态水印，只支持左上，左下，右上，右下四个位置，不支持文字换行
     *
     * @param context      上下文
     * @param targetBitmap 需要被添加水印的图片
     * @param watermark    添加的图片水印
     * @param title        添加的文本水印
     * @param gravity      添加文本水印的位置
     **/
    public static Bitmap addWater(Context context, Bitmap targetBitmap, Bitmap watermark,
                                  String title, int gravity) {
        int margin = DimensionUtil.dip2px(context, 15);
        int textHeight = DimensionUtil.dip2px(context, 18);
        if (targetBitmap == null) {
            return null;
        }
        int w = targetBitmap.getWidth();
        int h = targetBitmap.getHeight();
        // TODO: 2022/8/4 需要处理图片太大造成的内存超过的问题,这里我的图片很小所以不写相应代码了,待处理
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        // 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        // 在 0，0坐标开始画入src
        cv.drawBitmap(targetBitmap, 0, 0, null);
        Paint paint = new Paint();
        //加入图片
        if (watermark != null) {
            int ww = watermark.getWidth();
            int wh = watermark.getHeight();
            // 在src的左下角画入水印
            cv.drawBitmap(watermark, margin, h - wh - margin, paint);
        }
        //加入文字
        if (title != null) {
            //设置水印字体
            Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/华文新魏.ttf");
            TextPaint textPaint = new TextPaint();
            textPaint.setColor(WaterMarkSetting.getInstant().getWaterIntColor());
            textPaint.setTypeface(font);
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
        // 保存
        cv.save();
        // 存储
        cv.restore();
        return newb;
    }

}
