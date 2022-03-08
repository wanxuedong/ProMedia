package com.simple.filmfactory.ui;

import androidx.databinding.DataBindingUtil;

import com.simple.filmfactory.R;
import com.simple.filmfactory.databinding.ActivityAudioFactoryBinding;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.ui.filemanagement.FileManagementActivity;

/**
 * 音频处理工厂类
 **/
public class AudioFactoryActivity extends BaseActivity {

    private ActivityAudioFactoryBinding mainBinding;

    @Override
    protected void init() {
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_audio_factory);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        mainBinding.baseHead.getLeftImageView().setOnClickListener(this);
        mainBinding.baseHead.getRightImageView().setOnClickListener(this);
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id){
            case R.id.right_img:
                //选择视频文件
                startActivity(FileManagementActivity.class);
                break;
        }
    }
}
