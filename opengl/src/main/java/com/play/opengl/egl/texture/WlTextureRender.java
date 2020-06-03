package com.play.opengl.egl.texture;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.play.opengl.R;
import com.play.opengl.egl.base.WLEGLSurfaceView;
import com.play.opengl.egl.render.FboRender;
import com.play.opengl.egl.utils.WlShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 绘制图像流程示例
 **/
public class WlTextureRender implements WLEGLSurfaceView.WlGLRender {

    private Context context;


    /**
     * 顶点着色器
     **/
    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            -0.5f, -0.5f,
            0.5f, -0.5f,
            -0.5f, 0.5f,
            0.5f, 0.5f
    };
    private FloatBuffer vertexBuffer;

    /**
     * 片元着色器
     **/
    private float[] fragmentData = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f
    };
    private FloatBuffer fragmentBuffer;

    /**
     * 着色器最终组合成的编译链id
     **/
    private int program;
    private int vPosition;
    private int fPosition;
    private int sampler;
    /**
     * 普通纹理
     **/
    private int textureId;
    private int moreTextureId;
    /**
     * vbo的id
     **/
    private int vboId;
    /**
     * fbo的id
     **/
    private int fboId;
    /**
     * fbo纹理
     **/
    private int fboTextureId;

    /**
     * fbo操作类，传入纹理后进行处理
     **/
    private FboRender fboRender;

    /**
     * 矩阵，用于实现正交投影，保证图片不发生形变
     **/
    private int uMatrix;
    private float[] matrix = new float[16];

    /**
     * 屏幕的宽高(其实应该是控件的宽高)
     **/
    private int screenWidth = 0, screenHeight = 0;

    /**
     * 图片的宽高
     **/
    private float imgWidth = 526, imgHeight = 702;

    /**
     * 初始化着色器并分配内存
     **/
    public WlTextureRender(Context context) {
        this.context = context;
        fboRender = new FboRender(context);
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

    @Override
    public void onSurfaceCreated() {

        if (screenWidth == 0 || screenWidth == 0) {
            int[] size = WlShaderUtil.getScreenSie(context);
            screenWidth = size[0];
            screenHeight = size[1];
        }

        fboRender.onCreate();

        //加载Shader代码
        String vertexSource = WlShaderUtil.getRawResource(context, R.raw.vertex_shader_m);
        String fragmentSource = WlShaderUtil.getRawResource(context, R.raw.fragment_shader);

        //创建顶点和片元程序的编译链
        program = WlShaderUtil.createProgram(vertexSource, fragmentSource);

        //获取属性id
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        sampler = GLES20.glGetUniformLocation(program, "sTexture");
        uMatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        //创建vbo缓存
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        //绑定bvo并设置缓存大小
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        //给vbo设置着色器的数据
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        //解绑vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //创建纹理
        textureId = WlShaderUtil.loadTexture(context, R.drawable.androids);
        moreTextureId = WlShaderUtil.loadTexture(context, R.drawable.ghnl);
//        float[] ImgSize = WlShaderUtil.getImgSize(context, R.drawable.androids);
//        imgWidth = ImgSize[0];
//        imgHeight = ImgSize[1];

        //创建fbo纹理id
        int[] fboIds = new int[1];
        GLES20.glGenTextures(1, fboIds, 0);
        fboTextureId = fboIds[0];
        //绑定fbo纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);

        //创建fbo的id
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        //绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //激活纹理并赋值
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sampler, 0);

        //配置环绕模式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        //配置过滤模式，缩小，放大等
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //分配fbo内存
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, screenWidth, screenHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //把指定纹理绑定到fbo上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureId, 0);
        //判断fbo是否绑定成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("pro_music", "fbo binding wrong");
        } else {
            Log.e("pro_music", "fbo binding success");
        }

        //解绑普通纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        if (onRenderCreateListener != null) {
            onRenderCreateListener.onCreate(fboTextureId);
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        fboRender.onChange(width, height);
        Log.e("pro_music", "宽高数据  : " + imgHeight + " : " + imgWidth);

        //判断设备横竖状态，再根据图像宽高和屏幕宽高计算实际归一化坐标
        if (width > height) {
            float ratio = (height / imgHeight) * imgWidth;
            Matrix.orthoM(matrix, 0, -width / ratio, width / ratio, -1f, 1f, -1f, 1f);
        } else {
            float ratio = (width / imgWidth) * imgHeight;
            Matrix.orthoM(matrix, 0, -1, 1, -height / ratio, height / ratio, -1f, 1f);
        }

        //矩阵控制图像旋转，传入角度，以及需要旋转的x,y,z轴
        Matrix.rotateM(matrix, 0, 180, 0, 1, 0);
    }

    @Override
    public void onDrawFrame() {

        //清空画板并用颜色清图
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        //确认使用编译链
        GLES20.glUseProgram(program);

        //使用正交投影,保证了图片不会发生拉伸形变
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0);

        //绑定vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //绑定第一个纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //激活属性并进行赋值，需要注意的是，如果使用vbo，最后一个参数传入偏移量即可，如果没用vbo，就传入顶点和纹理缓存
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);
        //绘制图像
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //绑定第二个纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, moreTextureId);
        //激活属性并进行赋值
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                32);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);
        //绘制图像
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //解绑fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //fbo绘制
        fboRender.onDraw(fboTextureId);

    }

    private OnRenderCreateListener onRenderCreateListener;

    public void setOnRenderCreateListener(OnRenderCreateListener onRenderCreateListener) {
        this.onRenderCreateListener = onRenderCreateListener;
    }

    public interface OnRenderCreateListener {
        void onCreate(int textureId);
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
}
