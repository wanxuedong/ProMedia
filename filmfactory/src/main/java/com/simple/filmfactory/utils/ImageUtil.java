package com.simple.filmfactory.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.simple.filmfactory.utils.logutils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 图片/视频操作工具类
 *
 * @author wanxuedong  2019/1/2
 **/
public class ImageUtil {

    /**
     * 字节转bitmap
     **/
    public static Bitmap Bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     * todo 貌似没用，待优化
     * 注意：在拍照图片返回的时候是否进行了图片处理，比如说压缩（我就是进行了压缩），
     * 因为在压缩图片后，我们的图片属性已经改变，旋转角度默认为了0，
     * 所以我们在使用ExifInterface的时候，获取到的就是为0
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 对bitmap进行角度旋转
     **/
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

    /**
     * 保存Bitmap图片在SD卡中
     * 如果没有SD卡则存在手机中
     *
     * @param mBitmap 需要保存的Bitmap图片
     * @return 保存成功时返回图片的路径，失败时返回null
     */
    public static String savePhotoToSD(Bitmap mBitmap, Context context, String name) {
        FileOutputStream outStream = null;
//        String fileName = getPhotoFileName(context, "image", name);
        String fileName = FileUtil.getPath(null, null, ".png");
        try {
            outStream = new FileOutputStream(fileName);
            // 把数据写入文件，100表示不压缩
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            LogUtil.d("保存了一张图片到本地,路径 : " + fileName);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (mBitmap != null) {
                    mBitmap.recycle();
                }
                //广播通知系统相册刷新
                if (fileName != null && !"".equals(fileName)) {
                    //刷新相册
                    refreshImage(context, "image", new File(fileName), name);
                    //获取图片旋转角度
                    readPictureDegree(fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拍完照片或录制完视频需要刷新相册
     * 需要注意的是，如果我们刚刚生成的文件是存储在我们应用的缓存路径下，那就需要插入图库，再刷新图库
     * 如果直接存在sd目录下，直接刷新图库即可
     *
     * @param type 传入image刷新图片，传入video刷新视频
     * @param file 需要刷新的文件
     * @param name 需要刷新的文件的名称
     **/
    public static void refreshImage(Context context, String type, File file, String name) {
        if (context == null || file == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }
//        if ("image".equals(type)) {
//            //把文件插入到系统图库
//            try {
//                MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                        file.getAbsolutePath(), name, null);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        } else {
//            // 扫描本地mp4文件并添加到本地视频库
//            // TODO: 2020-01-03 刷新视频到图库无效果
//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            intent.setData(Uri.fromFile(file));
//            context.sendBroadcast(intent);
//        }
        //刷新图库
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    /**
     * 获取的时间格式
     */
    public static final String TIME_STYLE = "yyyyMMddHHmmss";
    /**
     * 图片类型
     */
    public static final String IMAGE_TYPE = ".png";
    /**
     * 图片类型
     */
    public static final String VIDEO_TYPE = ".mp4";
    /**
     * 存放拍摄图片/视频的文件夹
     */
    private static final String FILES_NAME = "AACamera";

    /**
     * 获取手机可存储路径
     *
     * @param context 上下文
     * @return 手机可存储路径
     */
    private static String getPhoneRootPath(Context context) {
        // 是否有SD卡
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            // 获取SD卡根目录
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            // 获取apk包下的缓存路径
            return context.getCacheDir().getPath();
        }
    }


}
