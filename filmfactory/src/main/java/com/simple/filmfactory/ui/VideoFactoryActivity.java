package com.simple.filmfactory.ui;

import androidx.databinding.DataBindingUtil;

import com.simple.filmfactory.R;
import com.simple.filmfactory.databinding.ActivityVideoFactoryBinding;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.ui.filemanagement.FileManagementActivity;
import com.simple.filmfactory.utils.CameraDetecte;
import com.simple.filmfactory.utils.PermissionsUtils;
import com.simple.filmfactory.utils.ToastUtil;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：图片视频处理示例
 **/
public class VideoFactoryActivity extends BaseActivity {

    private ActivityVideoFactoryBinding mainBinding;

    @Override
    protected void init() {
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_factory);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        mainBinding.baseHead.getLeftImageView().setOnClickListener(this);
        mainBinding.baseHead.getRightImageView().setOnClickListener(this);
        mainBinding.videoRecording.setOnClickListener(this);
        mainBinding.videoCrop.setOnClickListener(this);
        mainBinding.videoSplicing.setOnClickListener(this);
        mainBinding.videoWatermark.setOnClickListener(this);
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id) {
            case R.id.right_img:
                //选择视频文件
                startActivity(FileManagementActivity.class);
                break;
            case R.id.video_recording:
                //录制视频
                if (CameraDetecte.hasCamera(this)) {
                    if (PermissionsUtils.getCameraPermission(this)) {
                        if (PermissionsUtils.getStorgePermission(this)) {
                            if (PermissionsUtils.getRecordPermission(this)) {
                                startActivity(CameraActivity.class);
                            }
                        }
                    }
                } else {
                    ToastUtil.show("当前设备无可用相机");
                }
                break;
            case R.id.video_crop:
                //视频裁剪
                break;
            case R.id.video_splicing:
                //视频拼接
                break;
            case R.id.video_watermark:
                //添加水印
                break;
            default:
        }
    }

}
