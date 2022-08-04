package com.simple.filmfactory.egl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.simple.filmfactory.R;
import com.simple.filmfactory.egl.base.BaseEGLSurfaceView;
import com.simple.filmfactory.egl.base.ShaderUtil;
import com.simple.filmfactory.utils.DisplayUtil;
import com.simple.filmfactory.utils.logutils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 一：实现摄像头预览界面的处理
 * 利用创建的纹理id初始化一个SurfaceTexture并回调给预览界面展示
 * 摄像机通过camera.setPreviewTexture(surfaceTexture)绑定纹理
 * 二：添加水印
 * 通过fbo渲染文字或图片转成的Bitmap，并重新绘制一个纹理，达成水印的效果
 **/
public class WlCameraRender implements BaseEGLSurfaceView.WlGLRender, SurfaceTexture.OnFrameAvailableListener {

    private Context context;

    /**
     * 四个顶点着色器坐标
     **/
    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
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
    private int vboId;
    private int fboId;

    private int fboTextureid;
    private int cameraTextureid;

    private int umatrix;
    private float[] matrix = new float[16];

    private SurfaceTexture surfaceTexture;
    private OnSurfaceCreateListener onSurfaceCreateListener;

    private WlCameraFboRender wlCameraFboRender;


    private int screenWidth;
    private int screenHeight;

    public WlCameraRender(Context context) {
        this.context = context;

        screenWidth = DisplayUtil.getScreenWidth(context);
        screenHeight = DisplayUtil.getScreenHeight(context);

        wlCameraFboRender = new WlCameraFboRender(context);
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

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        this.onSurfaceCreateListener = onSurfaceCreateListener;
    }

    @Override
    public void onSurfaceCreated() {

        //初始化fbo
        wlCameraFboRender.onCreate();

        String vertexSource = ShaderUtil.getRawResource(context, R.raw.vertex_shader);
        String fragmentSource = ShaderUtil.getRawResource(context, R.raw.fragment_shader);

        program = ShaderUtil.createProgram(vertexSource, fragmentSource);
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        umatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        int[] vbos = new int[1];
        //创建vbo
        GLES20.glGenBuffers(1, vbos, 0);
        //指定vbo的唯一id
        vboId = vbos[0];

        //绑定vbo唯一id到缓冲区
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //开启一块用户定义的数据空间，用于存储数据，并且定义了数据管理模式
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        //将用户创建的数据绑定到缓冲区，此后可高效的使用这些数据
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        //以下俩步同上，事实上开辟空间和绑定数据可写成一行代码，这里为了方便理解进行了拆分
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //创建fbo
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        //绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureid = textureIds[0];

        //需要注意的是，只有一个纹理的时候，默认使用的是第一个纹理，即GLES20.GL_TEXTURE0，所以不用指定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureid);

        //设置环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //2d图形，图层层级0，rgba，宽，高，0，rgba，无符号字节类型，null只分配大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, screenWidth, screenHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //把纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureid, 0);
        //检查fbo绑定是否成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            LogUtil.e("WlCameraRender", "fbo binding wrong!");
        }

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


        int[] textureIdSeOs = new int[1];
        GLES20.glGenTextures(1, textureIdSeOs, 0);
        cameraTextureid = textureIdSeOs[0];

        //使用摄像头需要用特殊的纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureid);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(cameraTextureid);
        surfaceTexture.setOnFrameAvailableListener(this);

        if (onSurfaceCreateListener != null) {
            onSurfaceCreateListener.onSurfaceCreate(surfaceTexture, fboTextureid);
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }


    public void resetMatrix() {
        Matrix.setIdentityM(matrix, 0);
    }

    public void setAngle(float angle, float x, float y, float z) {
        Matrix.rotateM(matrix, 0, angle, x, y, z);
    }


    @Override
    public void onSurfaceChanged(int width, int height) {
        wlCameraFboRender.onChange(width, height);
    }

    @Override
    public void onDrawFrame() {

        surfaceTexture.updateTexImage();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);
        GLES20.glViewport(0, 0, screenWidth, screenHeight);
        //激活程序编译链
        GLES20.glUseProgram(program);

        //使用vbo，降低资源消耗
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //使用矩阵，用于调整画面方向
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);

        //绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        //绑定摄像头纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureid);

        //激活顶点属性，默认禁止
        GLES20.glEnableVertexAttribArray(vPosition);
        //告诉OpenGL该如何解析顶点数据
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        //最终的绘制代码，根据已经配置的着色器等绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑纹理和vbo
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //解绑fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        wlCameraFboRender.onDraw(fboTextureid);


    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    public interface OnSurfaceCreateListener {
        void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid);
    }

}
