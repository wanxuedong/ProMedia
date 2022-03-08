package com.simple.filmfactory.ui.filemanagement;

import androidx.databinding.DataBindingUtil;

import com.simple.filmfactory.R;
import com.simple.filmfactory.databinding.ActivityFileManagementBinding;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.ui.filemanagement.audio.VideoFileListActivity;
import com.simple.filmfactory.ui.filemanagement.video.AudioFileListActivity;

/**
 * 音视频文件管理
 **/
public class FileManagementActivity extends BaseActivity {

    private ActivityFileManagementBinding managementBinding;

    @Override
    protected void init() {
        managementBinding = DataBindingUtil.setContentView(this, R.layout.activity_file_management);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        managementBinding.baseHead.getLeftImageView().setOnClickListener(this);
        managementBinding.videoFile.setOnClickListener(this);
        managementBinding.audioFile.setOnClickListener(this);
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id) {
            case R.id.video_file:
                //打开已导入的视频文件列表
                startActivity(VideoFileListActivity.class);
                break;
            case R.id.audio_file:
                //打开已导入的音频文件列表
                startActivity(AudioFileListActivity.class);
                break;
        }
    }
}
