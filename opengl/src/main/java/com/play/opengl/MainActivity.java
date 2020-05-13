package com.play.opengl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.play.opengl.egl.simple.EglSurfaceActivity;
import com.play.opengl.egl.texture.TextureActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //基本使用
    public void baseTest(View view) {
        startActivity(new Intent(this, EglSurfaceActivity.class));
    }

    //图片渲染
    public void picRender(View view) {
        startActivity(new Intent(this, TextureActivity.class));
    }

}
