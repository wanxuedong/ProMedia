package com.simple.filmfactory.encodec;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Build;
import android.text.TextUtils;

import com.simple.filmfactory.R;
import com.simple.filmfactory.egl.base.BaseEGLSurfaceView;
import com.simple.filmfactory.egl.base.ShaderUtil;
import com.simple.filmfactory.egl.listener.GLRender;
import com.simple.filmfactory.utils.WaterMarkSetting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 相机录制渲染层，为了和相机预览分离处理
 * **/
public class EnCodecRender implements GLRender {

    private Context context;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            0f, 0f,
            0f, 0f,
            0f, 0f,
            0f, 0f
    };
    private FloatBuffer vertexBuffer;

    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer fragmentBuffer;

    private int program;
    private int vPosition;
    private int fPosition;
    private int textureid;

    private int vboId;

    private Bitmap bitmap;
    private int bitmapTextureid;

    public EnCodecRender(Context context, int textureid) {
        this.context = context;
        this.textureid = textureid;
        initWaterMark();
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragmentData);
        fragmentBuffer.position(0);

    }

    /**
     * 初始化摄像头预览页面水印配置
     * **/
    private void initWaterMark() {
        String waterString = WaterMarkSetting.getInstant().getWaterString();
        String waterColor = WaterMarkSetting.getInstant().getWaterColor();
        if (WaterMarkSetting.getInstant().isWaterMark()) {
            bitmap = ShaderUtil.createTextImage(context, waterString, 40, waterColor, "#00000000", 0);
            //获取宽高比
            float proportion = ShaderUtil.getTextProportion(waterString);
            //x轴绘制距离边界偏移距离,默认不变
            final float offset = 0.1f;
            //需要绘制的x轴上的距离大小,范围0.1f-0.9f
            float drawX = WaterMarkSetting.getInstant().getWaterSize() * waterString.length();
            float drawY = drawX * proportion;
            switch (WaterMarkSetting.getInstant().getWaterPosition()) {
                case 1:
                    //左上
                    vertexData[8] = -(1f - offset) - drawX;
                    vertexData[9] = (1f - offset);

                    vertexData[10] = -(1f - offset);
                    vertexData[11] = (1f - offset);

                    vertexData[12] = -(1f - offset) - drawX;
                    vertexData[13] = (1f - offset - drawY);

                    vertexData[14] = -(1f - offset);
                    vertexData[15] = (1f - offset - drawY);
                    break;
                case 2:
                    //左下
                    vertexData[8] = -(1f - offset) - drawX;
                    vertexData[9] = -(1f - offset);

                    vertexData[10] = -(1f - offset);
                    vertexData[11] = -(1f - offset);

                    vertexData[12] = -(1f - offset) - drawX;
                    vertexData[13] = -(1f - offset - drawY);

                    vertexData[14] = -(1f - offset);
                    vertexData[15] = -(1f - offset - drawY);
                    break;
                case 3:
                    //右上
                    vertexData[8] = (1f - offset) - drawX;
                    vertexData[9] = (1f - offset);

                    vertexData[10] = (1f - offset);
                    vertexData[11] = (1f - offset);

                    vertexData[12] = (1f - offset) - drawX;
                    vertexData[13] = (1f - offset - drawY);

                    vertexData[14] = (1f - offset);
                    vertexData[15] = (1f - offset - drawY);
                    break;
                case 4:
                    //右下
                    vertexData[8] = (1f - offset) - drawX;
                    vertexData[9] = -(1f - offset);

                    vertexData[10] = (1f - offset);
                    vertexData[11] = -(1f - offset);

                    vertexData[12] = (1f - offset) - drawX;
                    vertexData[13] = -(1f - offset - drawY);

                    vertexData[14] = (1f - offset);
                    vertexData[15] = -(1f - offset - drawY);
                    break;
                default:
            }
        }
    }

    @Override
    public void onSurfaceCreated() {

        //开启透明，不然水印会有背景
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        String vertexSource = ShaderUtil.getRawResource(context, R.raw.vertex_shader_screen);
        String fragmentSource = ShaderUtil.getRawResource(context, R.raw.fragment_shader_screen);

        program = ShaderUtil.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");

        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        bitmapTextureid = ShaderUtil.loadBitmapTexture(bitmap);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //水印
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTextureid);
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                32);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
}
