package com.simple.filmfactory.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.simple.filmfactory.R;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseActivity extends Activity implements View.OnClickListener {

    private long lastClickTime = 0;
    private final int CLICK_DURATION = 500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initView();
        initData();
        initEvent();
    }

    protected abstract void init();

    public void initView() {
    }

    public void initData() {
    }

    public void initEvent() {
    }

    public void startActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    public void startActivity(Class<?> cls, HashMap<String, String> map) {
        Intent intent = new Intent(this, cls);
        if (map != null && map.size() > 0) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        startActivity(intent);
    }

    public void startActivity(Class<?> cls, HashMap<String, String> map, int requestCode) {
        Intent intent = new Intent(this, cls);
        if (map != null && map.size() > 0) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        startActivity(intent, requestCode);
    }

    public void startActivity(Class<?> cls, int requestCode) {
        startActivity(new Intent(this, cls), requestCode);
    }

    private void startActivity(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public final void onClick(View v) {
        if (System.currentTimeMillis() - lastClickTime < CLICK_DURATION) {
            return;
        }
        lastClickTime = System.currentTimeMillis();
        switch (v.getId()) {
            case R.id.left_img:
                finish();
                break;
        }
        onClick(v.getId());
    }

    public void onClick(int id) {
    }

}
