package com.simple.filmfactory.ui.filemanagement.audio;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.simple.commondialog.DialogConfig;
import com.simple.commondialog.DialogUtils;
import com.simple.filmfactory.R;
import com.simple.filmfactory.adapter.FileAdapter;
import com.simple.filmfactory.bean.FileBean;
import com.simple.filmfactory.databinding.ActivityVideoFileListBinding;
import com.simple.filmfactory.sql.FileDataBase;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.utils.CallBack;
import com.simple.filmfactory.utils.FileBrowsUtils;
import com.simple.filmfactory.utils.SV;
import com.simple.filmfactory.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 视频文件列表
 **/
public class VideoFileListActivity extends BaseActivity implements BaseQuickAdapter.OnItemLongClickListener, CallBack {

    private ActivityVideoFileListBinding videoFileListBinding;

    private final int OPEN_VIDEO = 1;

    private List<FileBean> fileBeans = new ArrayList<>();
    private FileAdapter fileAdapter;

    private View deleteAllVideo;
    private View mediaInfoView;
    private DialogConfig centerConfig;

    @Override
    protected void init() {
        videoFileListBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_file_list);
    }

    @Override
    public void initData() {
        super.initData();
        fileAdapter = new FileAdapter(fileBeans);
        videoFileListBinding.videoFileList.setLayoutManager(new LinearLayoutManager(this));
        videoFileListBinding.videoFileList.setAdapter(fileAdapter);

        List<FileBean> newList = FileDataBase.getInstance().getFile("1");
        fileBeans.addAll(newList);
        fileAdapter.notifyDataSetChanged();

        if (fileBeans.size() == 0) {
            videoFileListBinding.baseHead.getRightImageView().setVisibility(View.GONE);
        } else {
            videoFileListBinding.baseHead.getRightImageView().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initEvent() {
        super.initEvent();
        videoFileListBinding.baseHead.getLeftImageView().setOnClickListener(this);
        videoFileListBinding.baseHead.getRightImageView().setOnClickListener(this);
        videoFileListBinding.addVideo.setOnClickListener(this);
        fileAdapter.setOnItemLongClickListener(this);
    }

    @Override
    public void onClick(int id) {
        super.onClick(id);
        switch (id) {
            case R.id.add_video:
                //添加视频到本地
                FileBrowsUtils.openFile(this, 1, OPEN_VIDEO);
                break;
            case R.id.right_img:
                //删除全部已添加的视频文件
                if (deleteAllVideo == null) {
                    deleteAllVideo = LayoutInflater.from(this).inflate(R.layout.dialog_delete_all, null);
                    centerConfig = new DialogConfig();
                    deleteAllVideo.findViewById(R.id.delete_cancel).setOnClickListener(this);
                    deleteAllVideo.findViewById(R.id.delete_sure).setOnClickListener(this);
                    centerConfig.setGravity(Gravity.CENTER);
                }
                DialogUtils.show("delete_all_video", this, deleteAllVideo, centerConfig);
                break;
            case R.id.delete_cancel:
                //取消删除全部文件
                DialogUtils.disMiss("delete_all_video");
                break;
            case R.id.delete_sure:
                //确认删除全部文件
                DialogUtils.disMiss("delete_all_video");
                FileDataBase.getInstance().deleteAllFile("1", this);
                fileBeans.clear();
                fileAdapter.notifyDataSetChanged();
                ToastUtil.show("删除成功!");
                videoFileListBinding.baseHead.getRightImageView().setVisibility(View.GONE);
                break;
            case R.id.media_delete:
                //删除其中一条文件数据
                Log.d("file_database", itemLongClickPosition + "");
                if (itemLongClickPosition != -1) {
                    FileDataBase.getInstance().deleteFile(fileBeans.get(itemLongClickPosition).getNickName(), this);
                    fileBeans.remove(itemLongClickPosition);
                    fileAdapter.notifyDataSetChanged();
                    ToastUtil.show("删除成功!");
                } else {
                    ToastUtil.show("数据错误，请刷新页面!");
                }
                DialogUtils.disMiss("media_info");
                itemLongClickPosition = -1;

                if (fileBeans.size() == 0) {
                    videoFileListBinding.baseHead.getRightImageView().setVisibility(View.GONE);
                } else {
                    videoFileListBinding.baseHead.getRightImageView().setVisibility(View.VISIBLE);
                }
                break;
            default:
        }
    }

    /**
     * 选择文件后返回
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_VIDEO && resultCode == Activity.RESULT_OK) {
            String path = "";
            Uri uri = data.getData();
            //4.4以后
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                path = FileBrowsUtils.getPath(this, uri);
            } else {//4.4以下下系统调用方法
                path = FileBrowsUtils.getRealPathFromURI(this, uri);
            }
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                if (!FileDataBase.getInstance().hasExist(path)) {
                    FileBean fileBean = FileBean.newInstance();
                    fileBean.setFileType("1");
                    fileBean.setNickName(file.getName());
                    fileBean.setFilePath(path);
                    fileBean.setFileSize(FileBrowsUtils.getFileSize(file));
                    FileDataBase.getInstance().add(fileBean, this);
                    fileBeans.add(fileBean);
                    fileAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.show("你已添加过该文件!");
                }
            } else {
                ToastUtil.show("无法识别该文件!");
            }

            if (fileBeans.size() == 0) {
                videoFileListBinding.baseHead.getRightImageView().setVisibility(View.GONE);
            } else {
                videoFileListBinding.baseHead.getRightImageView().setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogUtils.remove("delete_all_video");
        DialogUtils.remove("media_info");
    }

    /**
     * 当前长按的位置
     **/
    private int itemLongClickPosition = -1;

    @Override
    public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
        if (mediaInfoView == null) {
            mediaInfoView = LayoutInflater.from(this).inflate(R.layout.dialog_media_info, null);
            centerConfig = new DialogConfig();
            centerConfig.setGravity(Gravity.CENTER);
            mediaInfoView.findViewById(R.id.media_delete).setOnClickListener(this);
        }
        itemLongClickPosition = position;
        FileBean item = fileBeans.get(position);
        SV.set((TextView) mediaInfoView.findViewById(R.id.media_name), item.getNickName());
        SV.set((TextView) mediaInfoView.findViewById(R.id.media_size), item.getFileSize());
        SV.set((TextView) mediaInfoView.findViewById(R.id.media_time), item.getAddTime());
        SV.set((TextView) mediaInfoView.findViewById(R.id.media_position), item.getFilePath());
        DialogUtils.show("media_info", this, mediaInfoView, centerConfig);
        return false;
    }

    @Override
    public Object call(String... data) {
        Log.d(data[0], data[1] + data[2]);
        return null;
    }
}
