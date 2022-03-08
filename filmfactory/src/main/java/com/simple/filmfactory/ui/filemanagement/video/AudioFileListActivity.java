package com.simple.filmfactory.ui.filemanagement.video;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simple.filmfactory.R;
import com.simple.filmfactory.adapter.FileAdapter;
import com.simple.filmfactory.bean.FileBean;
import com.simple.filmfactory.databinding.ActivityAudioFileListBinding;
import com.simple.filmfactory.sql.FileDataBase;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.utils.CallBack;
import com.simple.filmfactory.utils.FileBrowsUtils;
import com.simple.filmfactory.utils.ToastUtil;
import com.simple.filmfactory.utils.threadXUtil.AbstractLife;
import com.simple.filmfactory.utils.threadXUtil.ThreadX;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频文件列表
 **/
public class AudioFileListActivity extends BaseActivity implements CallBack {

    private ActivityAudioFileListBinding audioFileListBinding;

    private final int OPEN_AUDIO = 1;

    private List<FileBean> fileBeans = new ArrayList<>();
    private FileAdapter fileAdapter;

    @Override
    protected void init() {
        audioFileListBinding = DataBindingUtil.setContentView(this, R.layout.activity_audio_file_list);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        audioFileListBinding.baseHead.getLeftImageView().setOnClickListener(this);
        audioFileListBinding.addAudio.setOnClickListener(this);
    }

    @Override
    public void initData() {
        super.initData();
        fileAdapter = new FileAdapter(fileBeans);
        audioFileListBinding.audioFileList.setLayoutManager(new LinearLayoutManager(this));
        audioFileListBinding.audioFileList.setAdapter(fileAdapter);

        List<FileBean> newList = FileDataBase.getInstance().getFile("2");
        fileBeans.addAll(newList);
        fileAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id) {
            case R.id.add_audio:
                //添加视频到本地
                FileBrowsUtils.openFile(this, 2, OPEN_AUDIO);
                break;
        }
    }

    private String path = "";

    /**
     * 选择文件后返回
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_AUDIO && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            //使用第三方应用打开
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                path = uri.getPath();
                return;
            }
            //4.4以后
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                path = FileBrowsUtils.getPath(this, uri);
            } else {//4.4以下下系统调用方法
                path = FileBrowsUtils.getRealPathFromURI(this, uri);
            }
            if (!TextUtils.isEmpty(path)) {
                Log.d("file_path", path);
                if (!FileDataBase.getInstance().hasExist(path)) {
                    FileBean fileBean = FileBean.newInstance();
                    fileBean.setFileType("2");
                    fileBean.setFilePath(path);
                    FileDataBase.getInstance().add(fileBean, this);
                    fileBeans.add(0, fileBean);
                    fileAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.show("你已添加过该文件!");
                }
            } else {
                ToastUtil.show("无法识别该文件!");
            }
        }
    }

    @Override
    public Object call(String... data) {
        Log.d(data[0], data[1] + data[2]);
        return null;
    }
}
