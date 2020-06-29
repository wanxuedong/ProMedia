package com.simple.filmfactory.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * 图片压缩工具
 * Bitmap所占用的内存 = 图片长度 x 图片宽度 x 一个像素点占用的字节数。
 * 3个参数，任意减少一个的值，就达到了压缩的效果
 **/
public class BitMapCompressionTool {

    /**
     * 质量压缩，不影响图片宽高和大小
     * rate建议取值40～80
     * 保持像素的前提下改变图片的位深及透明度
     * 能够让Bitmap转成的byte长度减小，方便二进制传输数据
     * rate越低，能够让图片青一块白一块
     **/
    private static Bitmap qualityCompress(Context context, int resource, int rate) {
        Bitmap bit = BitmapFactory.decodeResource(context.getResources(), resource);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = Integer.valueOf(rate);
        bit.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bm;
    }

    /**
     * 采样率压缩
     * inSampleSize表示压缩的倍数，建议取值1～10
     * 数字越大，能够让图片变的越来越模糊
     **/
    private static Bitmap samplingCompress(Context context, int resource) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //将inJustDecodeBounds设为true表示只加载图片的宽高信息，不加载图片信息，避免OOM
        options.inJustDecodeBounds = true;
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resource, options);
        options.inSampleSize = 10;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeResource(context.getResources(), resource, options);
        return bm;
    }

    /**
     * RGB565压缩格式
     * 通过改变图片的颜色模式减小图片的大小
     * 其中，A代表透明度；R代表红色；G代表绿色；B代表蓝色。
     * ALPHA_8
     * 表示8位Alpha位图,即A=8,一个像素点占用1个字节,它没有颜色,只有透明度，1个字节
     * RGB_565
     * 表示16位RGB位图,即R=5,G=6,B=5,它没有透明度,一个像素点占5+6+5=16位，2个字节
     * ARGB_4444
     * 表示16位ARGB位图，即A=4,R=4,G=4,B=4,一个像素点占4+4+4+4=16位，2个字节
     * ARGB_8888
     * 表示32位ARGB位图，即A=8,R=8,G=8,B=8,一个像素点占8+8+8+8=32位，4个字节
     **/
    private static Bitmap rgb565Compress(Context context, int resource) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resource, options);
        return bm;
    }

}
