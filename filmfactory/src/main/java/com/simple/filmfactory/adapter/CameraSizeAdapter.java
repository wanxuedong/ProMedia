package com.simple.filmfactory.adapter;

import android.view.View;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.simple.filmfactory.R;
import com.simple.filmfactory.bean.SizeBean;
import com.simple.filmfactory.utils.CallBack;

import java.util.List;

public class CameraSizeAdapter extends BaseQuickAdapter<SizeBean, BaseViewHolder> {

    private CallBack callBack;

    public CameraSizeAdapter(@Nullable List<SizeBean> data) {
        super(R.layout.item_camera_size, data);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected void convert(final BaseViewHolder helper, SizeBean item) {
        helper.setText(R.id.size_description, "[ " + item.getHeight() + ":" + item.getWidth() + " ]");
        if (item.isChose()) {
            helper.setImageResource(R.id.size_chose_status, R.drawable.size_chose_yes);
        } else {
            helper.setImageResource(R.id.size_chose_status, R.drawable.size_chose_not);
        }
        helper.getView(R.id.size_chose_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.call("click", helper.getLayoutPosition() + "");
                }
            }
        });
    }

}
