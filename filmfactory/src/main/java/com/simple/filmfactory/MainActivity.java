package com.simple.filmfactory;

import androidx.databinding.DataBindingUtil;

import com.simple.filmfactory.databinding.ActivityMainBinding;
import com.simple.filmfactory.ui.AudioFactoryActivity;
import com.simple.filmfactory.ui.VideoFactoryActivity;
import com.simple.filmfactory.ui.base.BaseActivity;

/**
 * 主界面
 **/
public class MainActivity extends BaseActivity {

    private ActivityMainBinding mainBinding;

    @Override
    protected void init() {
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        mainBinding.videoFactory.setOnClickListener(this);
        mainBinding.audioFactory.setOnClickListener(this);
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id) {
            case R.id.video_factory:
                //视频加工
                startActivity(VideoFactoryActivity.class);
                break;
            case R.id.audio_factory:
                //音频加工
                startActivity(AudioFactoryActivity.class);
                break;
            default:
        }
    }

}
