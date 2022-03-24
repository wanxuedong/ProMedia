package media.share.audioplayer.utils;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * 动态获取手机权限的工具类
 */

public class PermissionsUtils {

    /**
     * 存储权限申请code
     * **/
    private static final int PEMISSION_EXSTORGE = 1000;
    /**
     * 相机权限申请code
     * **/
    private static final int PEMISSION_CAMERA = 1050;
    /**
     * 定位权限申请code
     * **/
    private static final int PEMISSION_LOCATION = 1100;
    /**
     * 电话权限申请code
     **/
    private static final int PEMISSION_CALL = 1150;

    //获取存储权限/
    public static boolean getStorgePermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PEMISSION_EXSTORGE);
            return false;
        } else {
            //调用方法
            return true;
        }
    }

    //获取相机权限
    public static boolean getCameraPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, PEMISSION_CAMERA);
            return false;
        } else {
            //调用方法
            return true;
        }
    }

    //获取定位权限
    public static boolean getLocationPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PEMISSION_LOCATION);
            return false;
        } else {
            //调用方法
            return true;
        }
    }

    //获取拨打电话权限
    public static boolean getPhonePermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, PEMISSION_CALL);
            return false;
        } else {
            //调用方法
            return true;
        }
    }

    //获取悬浮框权限
    public static void getOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            }
        }
    }

    //判断gps是否打开
    public static boolean isGpsOpen(Activity activity) {
        LocationManager lm = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        if (lm == null){
            return false;
        }
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //获取录音权限
    public static boolean getRecordPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, PEMISSION_CALL);
            return false;
        } else {
            //调用方法
            return true;
        }
    }

}
