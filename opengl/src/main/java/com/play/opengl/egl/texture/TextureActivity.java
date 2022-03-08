package com.play.opengl.egl.texture;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.play.opengl.R;
import com.play.opengl.egl.multi.WlMultiSurfaceView;


public class TextureActivity extends Activity {

    private WlGLTextureView wlGLTextureView;
    private LinearLayout lyContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        wlGLTextureView = findViewById(R.id.texture_view);
        lyContent = findViewById(R.id.texture_content);

        wlGLTextureView.getWlTextureRender().setOnRenderCreateListener(new WlTextureRender.OnRenderCreateListener() {
            @Override
            public void onCreate(final int textureId) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (lyContent.getChildCount() > 0) {
                            lyContent.removeAllViews();
                        }

                        for (int i = 0; i < 3; i++) {
                            WlMultiSurfaceView wlMultiSurfaceView = new WlMultiSurfaceView(TextureActivity.this);
                            wlMultiSurfaceView.setTextureId(textureId, i);
                            wlMultiSurfaceView.setSurfaceAndEglContext(null, wlGLTextureView.getEglContext());

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp.width = 200;
                            lp.height = 300;
                            wlMultiSurfaceView.setLayoutParams(lp);

                            lyContent.addView(wlMultiSurfaceView);
                        }
                    }
                });

            }
        });
    }
}
