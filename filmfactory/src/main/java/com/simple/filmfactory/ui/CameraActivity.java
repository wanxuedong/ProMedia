package com.simple.filmfactory.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaFormat;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.simple.filmfactory.R;
import com.simple.filmfactory.bean.CameraSets;
import com.simple.filmfactory.databinding.ActivityCameraBinding;
import com.simple.filmfactory.encodec.WlBaseMediaEncoder;
import com.simple.filmfactory.encodec.WlMediaEncodec;
import com.simple.filmfactory.encodec.listener.OnMediaInfoListener;
import com.simple.filmfactory.encodec.listener.OnStatusChangeListener;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.utils.CallBack;
import com.simple.filmfactory.utils.CameraDetecte;
import com.simple.filmfactory.utils.CameraViewHelper;
import com.simple.filmfactory.utils.FileSaveUtil;
import com.simple.filmfactory.utils.FileUtil;
import com.simple.filmfactory.utils.ImageUtil;
import com.simple.filmfactory.utils.WaterMarkUtil;
import com.simple.filmfactory.utils.threadXUtil.AbstractLife;
import com.simple.filmfactory.utils.threadXUtil.ThreadX;
import com.simple.filmfactory.widget.lineview.GateView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.simple.filmfactory.utils.FileCataLog.BASE_NAME;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：拍照或录制视频
 */
public class CameraActivity extends BaseActivity implements GateView.OnNavigateLisenter {

    private ActivityCameraBinding cameraBinding;

    private List<String> gateList = new ArrayList<>();

    /**
     * 相机界面的设置
     **/
    private CameraSets cameraSets;

    /**
     * 当前是否选择了拍照选项
     **/
    private boolean isTakePhoto = true;

    /**
     * 是否在拍照聚焦中
     **/
    private boolean isFocus = false;

    /**
     * 当前是否处于摄像状态
     **/
    private boolean isTakeVideo = false;

    private WlMediaEncodec wlMediaEncodec;

    /**
     * 是否使用的是反面摄像头
     **/
    private boolean isBack = true;

    private PutPcmThread putPcmThread;

    /**
     * 当前录制的视频
     * **/
    private String currentVideo;

    /**
     * 拍照或录像的宽度分辨率
     * **/
    int cameraWidth = 720;

    /**
     * 拍照或录像的高度分辨率
     * **/
    int cameraHeight = 1280;

    /**
     * 是否开启了相机水印，默认不开启
     * **/
    private boolean isWaterOpen;

    @Override
    protected void init() {
        cameraBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
    }

    @Override
    public void initView() {
        super.initView();
    }

    @Override
    public void initData() {
        super.initData();
        gateList.add("拍照");
        gateList.add("录像");
        cameraBinding.cameraGuide.setNavigation(gateList);
        cameraSets = (CameraSets) FileSaveUtil.readSerializable("camera_setting.txt");
        refreshCameraSet();
        CameraViewHelper.autoToSize(this, cameraBinding.cameraView, cameraHeight, cameraWidth);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        cameraBinding.cameraToRecord.setOnClickListener(this);
        cameraBinding.cameraToFlip.setOnClickListener(this);
        cameraBinding.cameraGuide.setOnNavigateLisenter(this);
        cameraBinding.cameraAlbum.setOnClickListener(this);
        cameraBinding.baseHead.getLeftImageView().setOnClickListener(this);
        cameraBinding.baseHead.getRightImageView().setOnClickListener(this);
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id) {
            case R.id.camera_to_record:
                //开始拍照或录制视频
                start();
                break;
            case R.id.camera_to_flip:
                //切换摄像头
                cameraBinding.cameraView.switchCamera();
                isBack = !isBack;
                break;
            case R.id.camera_album:
                //进入系统相册
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    if (isTakePhoto){
                        intent.setType("image/*");
                    }else {
                        intent.setType("video/*");
                    }
                } else {
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (isTakePhoto) {
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    }else {
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*");
                    }
                }
                startActivityForResult(intent, 1);
                break;
            case R.id.right_img:
                //打开摄像头设置页面
                HashMap<String, String> map = new HashMap<>();
                map.put("isBack", isBack ? "yes" : "not");
                startActivity(CameraSettingActivity.class, map, 10000);
                break;
            default:
        }
    }

    /**
     * 开始拍照或者录制视频
     **/
    public void start() {
        refreshCameraSet();
        if (isTakePhoto) {
            //当前处于拍照模式
            if (!isFocus) {
                //先聚焦成功，再拍照
                isFocus = true;
                CameraDetecte.toFocus(cameraBinding.cameraView.getWlCamera().getCamera(), new CallBack() {
                    @Override
                    public Object call(String... content) {
                        if ("focus_on_success".equals(content[0])) {
                            cameraBinding.cameraView.getWlCamera().getCamera().takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(final byte[] data, Camera camera) {
                                    //需要注意的是，拍照后，预览界面会被卡住，需要调用下面代码强制刷新预览界面
                                    cameraBinding.cameraView.getWlCamera().switchCamera(cameraBinding.cameraView.getWlCamera().isBack);
                                    ThreadX.x().run(new AbstractLife() {
                                        @Override
                                        public void run() {
                                            super.run();
                                            //图片添加水印
                                            Bitmap bitmap = ImageUtil.rotateBitmap(ImageUtil.Bytes2Bitmap(data), isBack ? 90 : -90);
                                            Bitmap waterMap = BitmapFactory.decodeResource(getResources(), R.mipmap.dipian, null);
                                            if (isWaterOpen) {
                                                bitmap = WaterMarkUtil.addWater(CameraActivity.this, bitmap, waterMap, "内涵段子tv", Gravity.RIGHT | Gravity.BOTTOM);
                                            }
                                            final Bitmap finalBitmap = bitmap;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cameraBinding.cameraAlbum.setImageBitmap(finalBitmap);
                                                }
                                            });
                                            //将图片旋转后并刷新至系统图库
                                            ImageUtil.savePhotoToSD(bitmap, CameraActivity.this, null);
                                        }
                                    });
                                    isFocus = false;
                                }
                            });
                        }
                        return null;
                    }
                });
            }
        } else {
            //当前处于录像模式
            if (isTakeVideo) {
                //当前正在录像
                wlMediaEncodec.stopRecord();
                wlMediaEncodec = null;
                //停止并隐藏计时器
                cameraBinding.cameraTime.stop();
                cameraBinding.cameraTime.setVisibility(View.GONE);
                isTakeVideo = false;
            } else {
                //当前不在录像
                if (wlMediaEncodec == null) {
                    wlMediaEncodec = new WlMediaEncodec(this, cameraBinding.cameraView.getTextureId());
                    currentVideo = FileUtil.getPath(BASE_NAME, null, ".mp4");
                    wlMediaEncodec.initEnCodec(cameraBinding.cameraView.getEglContext(),currentVideo
                            , MediaFormat.MIMETYPE_VIDEO_AVC,
                            cameraHeight, cameraWidth, 44100, 2, 16);
                    wlMediaEncodec.setOnMediaInfoListener(new OnMediaInfoListener() {
                        @Override
                        public void onMediaTime(int times) {
                        }
                    });
                    wlMediaEncodec.setOnStatusChangeListener(new OnStatusChangeListener() {
                        @Override
                        public void onStatusChange(OnStatusChangeListener.STATUS status) {
                            if (status == OnStatusChangeListener.STATUS.START) {
//                                putPcmThread = new PutPcmThread(new WeakReference<CameraActivity>(CameraActivity.this));
//                                putPcmThread.start();
                            }else if (status == OnStatusChangeListener.STATUS.END){
                                //刷新录制完毕的视频到相册
                                ImageUtil.refreshImage(CameraActivity.this, "", new File(currentVideo), "");
                            }
                        }
                    });
                }
                wlMediaEncodec.startRecord();
                //启动并显示计时器
                cameraBinding.cameraTime.start();
                cameraBinding.cameraTime.setVisibility(View.VISIBLE);
                isTakeVideo = true;
            }
        }
    }

    /**
     * 刷新相机配置
     * **/
    private void refreshCameraSet() {
        //刷新拍照或录像的分辨率
        if (isBack){
            cameraWidth = cameraSets.getPreviewWidth();
            cameraHeight = cameraSets.getPreviewHeight();
        }else {
            cameraWidth = cameraSets.getSelfieWidth();
            cameraHeight = cameraSets.getSelfieHeight();
        }
        //水印是否打开
        isWaterOpen = cameraSets.isWaterOpen();
    }

    @Override
    public void click(int position) {
        switch (gateList.get(position - 1)) {
            case "拍照":
                isTakePhoto = true;
                cameraBinding.cameraToRecord.setImageResource(R.drawable.camera_take_photo);
                break;
            case "录像":
                isTakePhoto = false;
                cameraBinding.cameraToRecord.setImageResource(R.drawable.camera_take_video);
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10000) {
            cameraSets = (CameraSets) FileSaveUtil.readSerializable("camera_setting.txt");
            refreshCameraSet();
            CameraViewHelper.autoToSize(this, cameraBinding.cameraView, cameraHeight, cameraWidth);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cameraBinding.cameraView.previewAngle(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBinding.cameraView.onDestroy();
    }

    private static class PutPcmThread extends Thread {

        private boolean isExit;
        private WeakReference<CameraActivity> reference;

        public PutPcmThread(WeakReference<CameraActivity> reference) {
            this.reference = reference;
        }

        public void setExit(boolean exit) {
            isExit = exit;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            InputStream inputStream = null;
            try {
                int s_ = 44100 * 2 * (16 / 2);
                int bufferSize = s_ / 100;

                inputStream = reference.get().getAssets().open("mydream.pcm");
                byte[] buffer = new byte[bufferSize];
                int size = 0;
                while ((size = inputStream.read(buffer, 0, bufferSize)) != -1) {
                    try {
                        // 10毫秒写入一次
                        Thread.sleep(1000 / 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (reference.get().wlMediaEncodec == null || isExit) {
                        break;
                    }
                    reference.get().wlMediaEncodec.putPcmData(buffer, size, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
