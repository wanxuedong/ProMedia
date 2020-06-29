package com.simple.filmfactory.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import com.simple.filmfactory.egl.base.BaseEGLSurfaceView;
import com.simple.filmfactory.egl.base.EglHelper;
import com.simple.filmfactory.utils.logutils.LogUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

/**
 * 用于实现预览画面的录制
 * 需要注意的是功能基本上和BaseEGLSurfaceView一样，是为了保证预览和录制的一致性
 * 但同时也可以分别操作，并且录制时，启动编码线程
 **/
public abstract class WlBaseMediaEncoder {

    private Surface surface;
    private EGLContext eglContext;

    private int width;
    private int height;

    private MediaCodec videoEncodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferinfo;

    private MediaMuxer mediaMuxer;

    private WlEGLMediaThread wlEGLMediaThread;
    private VideoEnCodecThread videoEncodecThread;


    private BaseEGLSurfaceView.WlGLRender wlGLRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    private OnMediaInfoListener onMediaInfoListener;


    public WlBaseMediaEncoder(Context context) {
    }

    public void setRender(BaseEGLSurfaceView.WlGLRender wlGLRender) {
        this.wlGLRender = wlGLRender;
    }

    public void setmRenderMode(int mRenderMode) {
        if (wlGLRender == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public void initEnCodec(EGLContext eglContext, String savePath, String mimeType, int width, int height) {
        this.width = width;
        this.height = height;
        this.eglContext = eglContext;
        initMediaEnCodec(savePath, mimeType, width, height);
    }

    public void startRecord() {
        if (surface != null && eglContext != null) {
            wlEGLMediaThread = new WlEGLMediaThread(new WeakReference<WlBaseMediaEncoder>(this));
            videoEncodecThread = new VideoEnCodecThread(new WeakReference<WlBaseMediaEncoder>(this));
            wlEGLMediaThread.isCreate = true;
            wlEGLMediaThread.isChange = true;
            wlEGLMediaThread.start();
            videoEncodecThread.start();
        }
    }

    public void stopRecord() {
        if (wlEGLMediaThread != null && videoEncodecThread != null) {
            videoEncodecThread.exit();
            wlEGLMediaThread.onDestory();
            videoEncodecThread = null;
            wlEGLMediaThread = null;
        }
    }

    private void initMediaEnCodec(String savePath, String mimeType, int width, int height) {
        try {
            LogUtil.d("保存了一个视频到本地,路径 : " + savePath);
            mediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initVideoEnCodec(mimeType, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initVideoEnCodec(String mimeType, int width, int height) {
        try {
            videoBufferinfo = new MediaCodec.BufferInfo();
            if ((width & 1) == 1) {
                width--;
            }
            if ((height & 1) == 1) {
                height--;
            }
            videoFormat = MediaFormat.createVideoFormat(mimeType, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            videoEncodec = MediaCodec.createEncoderByType(mimeType);
            videoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            surface = videoEncodec.createInputSurface();

        } catch (IOException e) {
            e.printStackTrace();
            videoEncodec = null;
            videoFormat = null;
            videoBufferinfo = null;
        }

    }

    /**
     * 开始录制时，启动EGL环境，不断渲染界面并录制到本地
     **/
    static class WlEGLMediaThread extends Thread {
        private WeakReference<WlBaseMediaEncoder> encoder;
        private EglHelper eglHelper;
        private Object object;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        public WlEGLMediaThread(WeakReference<WlBaseMediaEncoder> encoder) {
            this.encoder = encoder;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(encoder.get().surface, encoder.get().eglContext);

            while (true) {
                if (isExit) {
                    release();
                    break;
                }

                if (isStart) {
                    if (encoder.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (encoder.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }
                onCreate();
                onChange(encoder.get().width, encoder.get().height);
                onDraw();
                isStart = true;
            }

        }

        private void onCreate() {
            if (isCreate && encoder.get().wlGLRender != null) {
                isCreate = false;
                encoder.get().wlGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && encoder.get().wlGLRender != null) {
                isChange = false;
                encoder.get().wlGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (encoder.get().wlGLRender != null && eglHelper != null) {
                encoder.get().wlGLRender.onDrawFrame();
                if (!isStart) {
                    encoder.get().wlGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();

            }
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        public void onDestory() {
            isExit = true;
            requestRender();
        }

        public void release() {
            if (eglHelper != null) {
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                encoder = null;
            }
        }
    }

    /**
     * 启动录制时，不断的将数据编码到本地文件中，产生录制文件
     **/
    static class VideoEnCodecThread extends Thread {
        private WeakReference<WlBaseMediaEncoder> encoder;

        private boolean isExit;

        private MediaCodec videoEncodec;
        private MediaFormat videoFormat;
        private MediaCodec.BufferInfo videoBufferinfo;
        private MediaMuxer mediaMuxer;

        private int videoTrackIndex;
        private long pts;


        public VideoEnCodecThread(WeakReference<WlBaseMediaEncoder> encoder) {
            this.encoder = encoder;
            videoEncodec = encoder.get().videoEncodec;
            videoFormat = encoder.get().videoFormat;
            videoBufferinfo = encoder.get().videoBufferinfo;
            mediaMuxer = encoder.get().mediaMuxer;
        }

        @Override
        public void run() {
            super.run();
            pts = 0;
            videoTrackIndex = -1;
            isExit = false;
            videoEncodec.start();
            while (true) {
                if (isExit) {

                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;

                    mediaMuxer.stop();
                    mediaMuxer.release();
                    mediaMuxer = null;


                    break;
                }

                //从输出队列中取出编码操作之后的数据
                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);

                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    videoTrackIndex = mediaMuxer.addTrack(videoEncodec.getOutputFormat());
                    mediaMuxer.start();
                } else {
                    while (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(videoBufferinfo.offset);
                        outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);

                        if (pts == 0) {
                            pts = videoBufferinfo.presentationTimeUs;
                        }
                        videoBufferinfo.presentationTimeUs = videoBufferinfo.presentationTimeUs - pts;

                        mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, videoBufferinfo);
                        if (encoder.get().onMediaInfoListener != null) {
                            encoder.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                        }

                        videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                    }
                }
            }

        }

        public void exit() {
            isExit = true;
        }

    }

    public interface OnMediaInfoListener {
        void onMediaTime(int times);
    }


}
