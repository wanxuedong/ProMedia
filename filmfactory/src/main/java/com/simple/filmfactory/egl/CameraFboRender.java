package com.simple.filmfactory.egl;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Build;
import android.text.TextUtils;

import com.simple.filmfactory.R;
import com.simple.filmfactory.egl.base.ShaderUtil;
import com.simple.filmfactory.utils.WaterMarkSetting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author wan
 * 创建日期：2022/08/05
 * 描述：单独处理fbo的纹理，用于添加水印等特效，提高了渲染效率
 **/
public class CameraFboRender {

    private Context context;

    /**
     * 8个顶点着色器坐标，除了绘制界面，还需要绘制水印
     **/
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

    /**
     * 四个片元着色器坐标
     **/
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
    private int sampler;

    private int vboId;

    private Bitmap bitmap;
    private int bitmapTextureid;

    public CameraFboRender(Context context) {
        this.context = context;
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

    public void onCreate() {

        //开启透明，不然水印会有背景
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        String vertexSource = ShaderUtil.getRawResource(context, R.raw.vertex_shader_screen);
        String fragmentSource = ShaderUtil.getRawResource(context, R.raw.fragment_shader_screen);

        program = ShaderUtil.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        sampler = GLES20.glGetUniformLocation(program, "sTexture");

        //创建vbo
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        //绑定vbo并初始化控件
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        if (bitmap != null) {
            bitmapTextureid = ShaderUtil.loadBitmapTexture(bitmap);
        }
    }

    private int width;
    private int height;

    public void onChange(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void onDraw(int textureId) {
        ////把窗口清除为当前颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //设置清除颜色
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        GLES20.glViewport(0, 0, width, height);

        GLES20.glUseProgram(program);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //通过vPosition激活对应的属性，才可以正常的读写数据
        GLES20.glEnableVertexAttribArray(vPosition);
        //用于设置顶点数据解析方式
        //第一个参数指定从索引开始取数据，与顶点着色器中layout(location=0)对应。
        //第二个参数指定顶点属性大小。
        //第三个参数指定数据类型。
        //第四个参数定义是否希望数据被标准化（归一化），只表示方向不表示大小。
        //第五个参数是步长（Stride），指定在连续的顶点属性之间的间隔。
        //第六个参数表示我们的位置数据在缓冲区起始位置的偏移量。
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 2 * 4,
                0);
        GLES20.glEnableVertexAttribArray(fPosition);
        //激活属性并进行赋值，需要注意的是，如果使用vbo，最后一个参数传入偏移量即可，如果没用vbo，就传入顶点和纹理缓存
        //这里是因为使用来vbo，缓存了顶点和片元的全部坐标，所以使用给片元坐标操作时，需要偏移整个顶点坐标的数据量
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 2 * 4,
                vertexData.length * 4);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        //绘制bitmap水印
        if (WaterMarkSetting.getInstant().isWaterMark()) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTextureid);
            GLES20.glEnableVertexAttribArray(vPosition);
            //这里的32指的是使用后面的4个顶点坐标来绘制水印
            GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 2 * 4,
                    32);
            GLES20.glEnableVertexAttribArray(fPosition);
            GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 2 * 4,
                    vertexData.length * 4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        }
    }

}
