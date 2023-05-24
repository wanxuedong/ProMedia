package com.simple.filmfactory.ui;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.databinding.DataBindingUtil;

import com.simple.filmfactory.R;
import com.simple.filmfactory.bean.CameraSets;
import com.simple.filmfactory.databinding.ActivityCameraSettingBinding;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.utils.FileSaveUtil;
import com.simple.filmfactory.utils.WaterMarkSetting;

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
        settingBinding.waterTxt.setText(cameraSets.getWaterString());
        selectWaterPosition(cameraSets.getWaterPosition());
        selectWaterSize(cameraSets.getWaterSize());
        selectWaterColor(cameraSets.getWaterColor());
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
        settingBinding.waterTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                cameraSets.setWaterString(settingBinding.waterTxt.getText().toString().trim());
                FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
            }
        });
        settingBinding.waterPositionOne.setOnClickListener(this);
        settingBinding.waterPositionTwo.setOnClickListener(this);
        settingBinding.waterPositionThree.setOnClickListener(this);
        settingBinding.waterPositionFor.setOnClickListener(this);
        settingBinding.waterSizeOne.setOnClickListener(this);
        settingBinding.waterSizeTwo.setOnClickListener(this);
        settingBinding.waterSizeThree.setOnClickListener(this);
        settingBinding.waterColorOne.setOnClickListener(this);
        settingBinding.waterColorTwo.setOnClickListener(this);
        settingBinding.waterColorThree.setOnClickListener(this);
        settingBinding.waterColorFor.setOnClickListener(this);
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
                    WaterMarkSetting.getInstant().setWaterMark(false);
                    settingBinding.watermarkStatus.setImageResource(R.drawable.chose_not);
                } else {
                    cameraSets.setWaterOpen(true);
                    WaterMarkSetting.getInstant().setWaterMark(true);
                    settingBinding.watermarkStatus.setImageResource(R.drawable.chose_yes);
                }
                FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
                break;
            case R.id.water_size_one:
                selectWaterSize(1);
                break;
            case R.id.water_size_two:
                selectWaterSize(2);
                break;
            case R.id.water_size_three:
                selectWaterSize(3);
                break;
            case R.id.water_position_one:
                selectWaterPosition(1);
                break;
            case R.id.water_position_two:
                selectWaterPosition(2);
                break;
            case R.id.water_position_three:
                selectWaterPosition(3);
                break;
            case R.id.water_position_for:
                selectWaterPosition(4);
                break;
            case R.id.water_color_one:
                selectWaterColor(1);
                break;
            case R.id.water_color_two:
                selectWaterColor(2);
                break;
            case R.id.water_color_three:
                selectWaterColor(3);
                break;
            case R.id.water_color_for:
                selectWaterColor(4);
                break;
            default:
        }
    }

    private void selectWaterPosition(int position){
        resetWaterPosition();
        switch (position){
            case 1:
                settingBinding.waterPositionOne.setBackgroundResource(R.drawable.select_bg);
                break;
            case 2:
                settingBinding.waterPositionTwo.setBackgroundResource(R.drawable.select_bg);
                break;
            case 3:
                settingBinding.waterPositionThree.setBackgroundResource(R.drawable.select_bg);
                break;
            case 4:
                settingBinding.waterPositionFor.setBackgroundResource(R.drawable.select_bg);
                break;
            default:
        }
        cameraSets.setWaterPosition(position);
        FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
    }

    private void resetWaterPosition() {
        settingBinding.waterPositionOne.setBackgroundResource(R.drawable.unselect_bg);
        settingBinding.waterPositionTwo.setBackgroundResource(R.drawable.unselect_bg);
        settingBinding.waterPositionThree.setBackgroundResource(R.drawable.unselect_bg);
        settingBinding.waterPositionFor.setBackgroundResource(R.drawable.unselect_bg);
    }

    private void selectWaterSize(int size){
        resetWaterSize();
        switch (size){
            case 1:
                settingBinding.waterSizeOne.setBackgroundResource(R.drawable.select_bg);
                break;
            case 2:
                settingBinding.waterSizeTwo.setBackgroundResource(R.drawable.select_bg);
                break;
            case 3:
                settingBinding.waterSizeThree.setBackgroundResource(R.drawable.select_bg);
                break;
            default:
        }
        cameraSets.setWaterSize(size);
        FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
    }

    private void resetWaterSize() {
        settingBinding.waterSizeOne.setBackgroundResource(R.drawable.unselect_bg);
        settingBinding.waterSizeTwo.setBackgroundResource(R.drawable.unselect_bg);
        settingBinding.waterSizeThree.setBackgroundResource(R.drawable.unselect_bg);
    }

    private void selectWaterColor(int position){
        resetWaterColor();
        switch (position){
            case 1:
                settingBinding.waterColorOne.setBackgroundResource(R.drawable.select_bg);
                break;
            case 2:
                settingBinding.waterColorTwo.setBackgroundResource(R.drawable.select_bg);
                break;
            case 3:
                settingBinding.waterColorThree.setBackgroundResource(R.drawable.select_bg);
                break;
            case 4:
                settingBinding.waterColorFor.setBackgroundResource(R.drawable.select_bg);
                break;
            default:
        }
        cameraSets.setWaterColor(position);
        FileSaveUtil.saveSerializable("camera_setting.txt", cameraSets);
    }

    private void resetWaterColor() {
        settingBinding.waterColorOne.setBackgroundResource(R.drawable.unselect_bg);
        settingBinding.waterColorTwo.setBackgroundResource(R.drawable.unselect_bg);
        settingBinding.waterColorThree.setBackgroundResource(R.drawable.unselect_bg);
        settingBinding.waterColorFor.setBackgroundResource(R.drawable.unselect_bg);
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
