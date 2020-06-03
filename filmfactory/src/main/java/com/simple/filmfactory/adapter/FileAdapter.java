package com.simple.filmfactory.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.simple.filmfactory.R;
import com.simple.filmfactory.bean.FileBean;

import java.util.List;

/**
 * 音视频文件列表
 **/
public class FileAdapter extends BaseQuickAdapter<FileBean, BaseViewHolder> {

    public FileAdapter(@Nullable List<FileBean> data) {
        super(R.layout.item_file, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, FileBean item) {
        if ("1".equals(item.getFileType())) {
            helper.setImageResource(R.id.file_type, R.mipmap.shipin);
        } else {
            helper.setImageResource(R.id.file_type, R.mipmap.yinpin);
        }
        helper.setText(R.id.file_name, item.getNickName());
        helper.setText(R.id.file_time, item.getAddTime());
        helper.setText(R.id.file_size, item.getFileSize());
    }

}
