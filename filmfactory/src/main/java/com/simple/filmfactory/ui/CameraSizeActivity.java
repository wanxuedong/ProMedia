package com.simple.filmfactory.ui;

import android.content.Intent;
import android.hardware.Camera;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simple.filmfactory.R;
import com.simple.filmfactory.adapter.CameraSizeAdapter;
import com.simple.filmfactory.bean.SizeBean;
import com.simple.filmfactory.databinding.ActivityCameraSizeBinding;
import com.simple.filmfactory.ui.base.BaseActivity;
import com.simple.filmfactory.utils.CallBack;
import com.simple.filmfactory.utils.CameraDetecte;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：选择照片分辨率
 *
 * todo 当选择某些分辨率时，点击录像会崩溃，待处理
 **/
public class CameraSizeActivity extends BaseActivity implements CallBack {

    private ActivityCameraSizeBinding cameraSizeBinding;

    /**
     * 正面或反面支持的尺寸列表
     **/
    List<Camera.Size> supportList;

    private CameraSizeAdapter cameraSizeAdapter;
    private List<SizeBean> sizeBeans = new ArrayList<>();

    /**
     * 是否使用的是反面摄像头
     **/
    private boolean isBack;

    @Override
    protected void init() {
        cameraSizeBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera_size);
    }

    @Override
    public void initData() {
        super.initData();
        isBack = "yes".endsWith(getIntent().getStringExtra("isBack")) ? true : false;
        supportList = CameraDetecte.getCameraSupportSize(isBack, null);
        for (int i = 0; i < supportList.size(); i++) {
            sizeBeans.add(new SizeBean(supportList.get(i).width, supportList.get(i).height));
        }
        cameraSizeAdapter = new CameraSizeAdapter(sizeBeans);
        cameraSizeAdapter.setCallBack(this);
        cameraSizeBinding.cameraSizeList.setLayoutManager(new LinearLayoutManager(this));
        cameraSizeBinding.cameraSizeList.setAdapter(cameraSizeAdapter);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        cameraSizeBinding.baseHead.getLeftImageView().setOnClickListener(this);
    }

    @Override
    public Object call(String... data) {
        if ("click".endsWith(data[0])) {
            int position = Integer.parseInt(data[1]);
            for (int i = 0; i < sizeBeans.size(); i++) {
                if (position == i) {
                    Intent intent = getIntent();
                    intent.putExtra("previewWidth", sizeBeans.get(i).getWidth());
                    intent.putExtra("previewHeight", sizeBeans.get(i).getHeight());
                    setResult(10001, intent);
                    sizeBeans.get(i).setChose(true);
                } else {
                    sizeBeans.get(i).setChose(false);
                }
            }
            cameraSizeAdapter.notifyDataSetChanged();
        }
        return null;
    }
}
