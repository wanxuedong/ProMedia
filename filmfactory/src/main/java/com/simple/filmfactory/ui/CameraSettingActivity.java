package com.simple.filmfactory.ui;

import android.content.Intent;
import android.util.Log;

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
     * 是否使用的是反面摄像头
     **/
    private boolean isBack;

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
        isBack = "yes".endsWith(getIntent().getStringExtra("isBack")) ? true : false;
        cameraSets = (CameraSets) FileSaveUtil.readSerializable("camera_setting.txt");
        if (cameraSets == null) {
            cameraSets = new CameraSets();
        }
        if (cameraSets.isWaterOpen()) {
            settingBinding.watermarkStatus.setImageResource(R.drawable.chose_yes);
        } else {
            settingBinding.watermarkStatus.setImageResource(R.drawable.chose_not);
        }
        if (isBack) {
            settingBinding.powerSize.setText(cameraSets.getPreviewHeight() + " x " + cameraSets.getPreviewWidth());
        } else {
            settingBinding.powerSize.setText(cameraSets.getSelfieHeight() + " x " + cameraSets.getSelfieWidth());
        }
    }

    @Override
    public void initEvent() {
        super.initEvent();
        settingBinding.baseHead.getLeftImageView().setOnClickListener(this);
        settingBinding.changeSize.setOnClickListener(this);
        settingBinding.watermarkStatus.setOnClickListener(this);
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id) {
            case R.id.change_size:
                //设置拍照或录像的分辨率
                HashMap<String, String> map = new HashMap<>();
                map.put("isBack", isBack ? "yes" : "not");
                startActivity(CameraSizeActivity.class, map, 10000);
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
        }
    }

    /**
     * 选择了其他分辨率并返回
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10000 && resultCode == 10001) {
            int previewWidth = data.getIntExtra("previewWidth", 0);
            int previewHeight = data.getIntExtra("previewHeight", 0);
            if (previewWidth == 0 || previewHeight == 0) {
                return;
            }
            if (isBack) {
                cameraSets.setPreviewWidth(previewWidth);
                cameraSets.setPreviewHeight(previewHeight);
            } else {
                cameraSets.setSelfieWidth(previewWidth);
                cameraSets.setSelfieHeight(previewHeight);
            }
            settingBinding.powerSize.setText(previewHeight + " x " + previewWidth);
            FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
        }
    }

}
