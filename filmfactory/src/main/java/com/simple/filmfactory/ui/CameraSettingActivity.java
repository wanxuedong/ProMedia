package com.simple.filmfactory.ui;

import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import com.simple.filmfactory.R;
import com.simple.filmfactory.bean.CameraSets;
import com.simple.filmfactory.databinding.ActivityCameraSettingBinding;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.utils.FileSaveUtil;

import java.util.HashMap;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：相机参数设置页面
 **/
public class CameraSettingActivity extends BaseActivity {

    private ActivityCameraSettingBinding settingBinding;

    /**
     * 相机界面的设置
     **/
    private CameraSets cameraSets;

    @Override
    protected void init() {
        settingBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera_setting);
    }

    @Override
    public void initData() {
        super.initData();
        cameraSets = (CameraSets) FileSaveUtil.readSerializable("camera_setting.txt");
        if (cameraSets == null) {
            cameraSets = new CameraSets();
        }
        if (cameraSets.isWaterOpen()) {
            settingBinding.watermarkStatus.setImageResource(R.drawable.chose_yes);
        } else {
            settingBinding.watermarkStatus.setImageResource(R.drawable.chose_not);
        }
        settingBinding.backPictureSize.setText(cameraSets.getBackPictureHeight() + " x " + cameraSets.getBackPictureWidth());
        settingBinding.backVideoSize.setText(cameraSets.getBackVideoHeight() + " x " + cameraSets.getBackVideoWidth());
        settingBinding.frontPictureSize.setText(cameraSets.getFrontPictureHeight() + " x " + cameraSets.getFrontPictureWidth());
        settingBinding.frontVideoSize.setText(cameraSets.getFrontVideoHeight() + " x " + cameraSets.getFrontVideoWidth());
    }

    @Override
    public void initEvent() {
        super.initEvent();
        settingBinding.baseHead.getLeftImageView().setOnClickListener(this);
        settingBinding.backPictureHolder.setOnClickListener(this);
        settingBinding.backVideoHolder.setOnClickListener(this);
        settingBinding.frontPictureHolder.setOnClickListener(this);
        settingBinding.frontVideoHolder.setOnClickListener(this);
        settingBinding.watermarkStatus.setOnClickListener(this);
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id) {
            case R.id.back_picture_holder:
                //设置拍照的后置分辨率
                HashMap<String, String> backPictureMap = new HashMap<>();
                backPictureMap.put("sizeType", "picture");
                backPictureMap.put("selectSize", cameraSets.getBackPictureHeight() + ":" + cameraSets.getBackPictureWidth());
                startActivity(CameraSizeActivity.class, backPictureMap, 10000);
                break;
            case R.id.back_video_holder:
                //设置录像的后置分辨率
                HashMap<String, String> backVideoMap = new HashMap<>();
                backVideoMap.put("sizeType", "video");
                backVideoMap.put("selectSize", cameraSets.getBackVideoHeight() + ":" + cameraSets.getBackVideoWidth());
                startActivity(CameraSizeActivity.class, backVideoMap, 20000);
                break;
            case R.id.front_picture_holder:
                //设置拍照的前置分辨率
                HashMap<String, String> frontPictureMap = new HashMap<>();
                frontPictureMap.put("sizeType", "picture");
                frontPictureMap.put("selectSize", cameraSets.getFrontPictureHeight() + ":" + cameraSets.getFrontPictureWidth());
                startActivity(CameraSizeActivity.class, frontPictureMap, 10001);
                break;
            case R.id.front_video_holder:
                //设置录像的前置分辨率
                HashMap<String, String> frontVideoMap = new HashMap<>();
                frontVideoMap.put("sizeType", "video");
                frontVideoMap.put("selectSize", cameraSets.getFrontVideoHeight() + ":" + cameraSets.getFrontVideoWidth());
                startActivity(CameraSizeActivity.class, frontVideoMap, 20001);
                break;
            case R.id.watermark_status:
                //是否保存水印
                if (cameraSets.isWaterOpen()) {
                    cameraSets.setWaterOpen(false);
                    settingBinding.watermarkStatus.setImageResource(R.drawable.chose_not);
                } else {
                    cameraSets.setWaterOpen(true);
                    settingBinding.watermarkStatus.setImageResource(R.drawable.chose_yes);
                }
                FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
                break;
            default:
        }
    }

    /**
     * 选择了其他分辨率并返回
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10000 && resultCode == 10001) {
            //后置拍照
            int width = data.getIntExtra("previewWidth", 0);
            int height = data.getIntExtra("previewHeight", 0);
            if (width == 0 || height == 0) {
                return;
            }
            cameraSets.setBackPictureWidth(width);
            cameraSets.setBackPictureHeight(height);
            settingBinding.backPictureSize.setText(height + " x " + width);
            FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
        } else if (requestCode == 10001 && resultCode == 10001) {
            //前置拍照
            int width = data.getIntExtra("previewWidth", 0);
            int height = data.getIntExtra("previewHeight", 0);
            if (width == 0 || height == 0) {
                return;
            }
            cameraSets.setFrontPictureWidth(width);
            cameraSets.setFrontPictureHeight(height);
            settingBinding.frontPictureSize.setText(height + " x " + width);
            FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
        }else if (requestCode == 20000 && resultCode == 10001) {
            //后置录像
            int width = data.getIntExtra("previewWidth", 0);
            int height = data.getIntExtra("previewHeight", 0);
            if (width == 0 || height == 0) {
                return;
            }
            cameraSets.setBackVideoWidth(width);
            cameraSets.setBackVideoHeight(height);
            settingBinding.backVideoSize.setText(height + " x " + width);
            FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
        }else if (requestCode == 20001 && resultCode == 10001) {
            //前置录像
            int width = data.getIntExtra("previewWidth", 0);
            int height = data.getIntExtra("previewHeight", 0);
            if (width == 0 || height == 0) {
                return;
            }
            cameraSets.setFrontVideoWidth(width);
            cameraSets.setFrontVideoHeight(height);
            settingBinding.frontVideoSize.setText(height + " x " + width);
            FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
        }
    }

}
