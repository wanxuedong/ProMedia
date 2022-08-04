package com.simple.filmfactory.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import com.simple.filmfactory.egl.base.BaseEGLSurfaceView;
import com.simple.filmfactory.encodec.listener.OnMediaInfoListener;
import com.simple.filmfactory.encodec.listener.OnStatusChangeListener;
import com.simple.filmfactory.encodec.mediathread.AudioEnCodecThread;
import com.simple.filmfactory.encodec.mediathread.MediaMuxerThread;
import com.simple.filmfactory.encodec.mediathread.VideoEnCodecThread;
import com.simple.filmfactory.encodec.mediathread.EGLMediaThread;
import com.simple.filmfactory.utils.logutils.LogUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.egl.EGLContext;

/**
 * 用于实现预览画面的录制
 * 需要注意的是功能基本上和BaseEGLSurfaceView一样，是为了保证预览和录制的一致性
 * 但同时也可以分别操作，并且录制时，启动编码线程
 **/
public abstract class WlBaseMediaEncoder {

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    public int mRenderMode = RENDERMODE_CONTINUOUSLY;

    public Surface surface;
    public EGLContext eglContext;

    public int width;
    public int height;

    public MediaCodec videoEncodec;
    public MediaFormat videoFormat;
    public MediaCodec.BufferInfo videoBufferinfo;

    public MediaCodec.BufferInfo mAudioBuffInfo;
    public MediaCodec audioEncodec;
    private int channel, sampleRate, sampleBit;

    public MediaMuxer mediaMuxer;

    private EGLMediaThread EGLMediaThread;
    private MediaMuxerThread mediaMuxerThread;
    private VideoEnCodecThread videoEncodecThread;
    private AudioEnCodecThread audioEnCodecThread;

    public boolean encodeStart;
    public boolean audioExit;
    public boolean videoExit;


    public BaseEGLSurfaceView.WlGLRender wlGLRender;

    public OnMediaInfoListener onMediaInfoListener;

    /**
     * 视频准备好合成
     **/
    public CountDownLatch videoDownLatch = new CountDownLatch(1);

    /**
     * 音频准备好合成
     **/
    public CountDownLatch audioDownLatch = new CountDownLatch(1);

    /**
     * 音视频合成开始
     **/
    public CountDownLatch startDownLatch = new CountDownLatch(2);

    /**
     * 音视频合成结束
     **/
    public CountDownLatch stopDownLatch = new CountDownLatch(2);

    /**
     * 录制状态监听
     **/
    public OnStatusChangeListener onStatusChangeListener;

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

    public void initEnCodec(EGLContext eglContext, String savePath, String mimeType, int width, int height, int sampleRate, int channel, int sampleBit) {
        this.width = width;
        this.height = height;
        this.eglContext = eglContext;
        this.sampleRate = sampleRate;
        this.sampleBit = sampleBit;
        this.channel = channel;
        initMediaEnCodec(savePath, mimeType, width, height);
    }

    /**
     * 开始录制
     **/
    public void startRecord() {
        if (surface != null && eglContext != null) {
            EGLMediaThread = new EGLMediaThread(new WeakReference<WlBaseMediaEncoder>(this));
            mediaMuxerThread = new MediaMuxerThread(new WeakReference<WlBaseMediaEncoder>(this));
            videoEncodecThread = new VideoEnCodecThread(new WeakReference<WlBaseMediaEncoder>(this));
            audioEnCodecThread = new AudioEnCodecThread(new WeakReference<>(this));

            EGLMediaThread.isCreate = true;
            EGLMediaThread.isChange = true;

            if (onStatusChangeListener != null) {
                onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.START);
            }
            videoDownLatch = new CountDownLatch(1);
            audioDownLatch = new CountDownLatch(1);
            startDownLatch = new CountDownLatch(2);
            stopDownLatch = new CountDownLatch(2);
            EGLMediaThread.start();
            mediaMuxerThread.start();
            videoEncodecThread.start();
            audioEnCodecThread.start();
        }
    }

    /**
     * 停止录制
     **/
    public void stopRecord() {
        if (EGLMediaThread != null && videoEncodecThread != null) {
            videoEncodecThread.exit();
            EGLMediaThread.onDestroy();
            videoEncodecThread = null;
            EGLMediaThread = null;
        }

        if (audioEnCodecThread != null) {
            audioEnCodecThread.exit();
            audioEnCodecThread = null;
        }
    }

    /**
     * 初始化音视频
     **/
    private void initMediaEnCodec(String savePath, String mimeType, int width, int height) {
        try {
            LogUtil.d("保存了一个视频到本地,路径 : " + savePath);
            mediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            //视频初始化
            initVideoEnCodec(mimeType, width, height);
            //音频初始化
            initAudioEncoder(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化视频
     *
     * @param mimeType 视频格式
     * @param width    视频宽度
     * @param height   视频高度
     **/
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
     * 初始化音频
     *
     * @param mineType   视频格式
     * @param sampleRate 采样率
     * @param channel    通道数
     **/
    private void initAudioEncoder(String mineType, int sampleRate, int channel) {
        try {
            audioEncodec = MediaCodec.createEncoderByType(mineType);
            MediaFormat audioFormat = MediaFormat.createAudioFormat(mineType, sampleRate, channel);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096);
            audioEncodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mAudioBuffInfo = new MediaCodec.BufferInfo();
        } catch (IOException e) {
            e.printStackTrace();
            audioEncodec = null;
            mAudioBuffInfo = null;
        }
    }

    /**
     * 将PCM数据写入到MediaCodeC中
     *
     * @param buffer  需要写入的PCM数据
     * @param size    写入buffer数据的大小
     * @param fillAll true，需要将全部的PCM数据输入到MediaCodeC中，会导致视频结束，音频接着播放的情况，比如4s视频，10s音频的情况
     *                false，根据视频长度，存储和视频时间长度一样的PCM数据
     *                所以为了保证视频和音频播放长度一直，需要同步录制视频和音频，或者校验视频和音频长度后，再进行合成
     **/
    public void putPcmData(byte[] buffer, int size, boolean fillAll) {
        if (audioEnCodecThread != null && !audioEnCodecThread.isExit && buffer != null && size > 0) {
            //循环复制的次数
            int count = 1;
            //剩余未复制完的字节长度,当为-1时，表示数据复制完毕
            int surplusSize = 0;
            //本次循环中，需要复制的字节头部位置
            int startSize = 0;
            //本次循环中，需要复制的字节尾部位置
            int endSize = 0;
            while (surplusSize > -1) {
                int inputBufferIndex = audioEncodec.dequeueInputBuffer(0);
                if (inputBufferIndex >= 0) {
                    ByteBuffer byteBuffer = audioEncodec.getInputBuffers()[inputBufferIndex];
                    byteBuffer.clear();
                    int capacity = byteBuffer.capacity();
                    if (buffer.length > byteBuffer.capacity() * count) {
                        surplusSize = buffer.length - capacity * count;
                        endSize = capacity * count;
                    } else {
                        surplusSize = -1;
                        endSize = buffer.length;
                    }
                    startSize = capacity * (count - 1);
                    byte[] temporary = Arrays.copyOfRange(buffer, startSize, endSize);
                    byteBuffer.put(temporary);
                    long pts = getAudioPts(endSize - startSize, sampleRate, channel, sampleBit);
                    audioEncodec.queueInputBuffer(inputBufferIndex, 0, endSize - startSize, pts, 0);
                    count++;
                    if (!fillAll) {
                        surplusSize = -1;
                    }
                }
            }
        }
    }

    private long audioPts;

    //176400
    private long getAudioPts(int size, int sampleRate, int channel, int sampleBit) {
        audioPts += (long) (1.0 * size / (sampleRate * channel * (sampleBit / 8)) * 1000000.0);
        return audioPts;
    }

    /**
     * 设置录制时长监听
     *
     * @param onMediaInfoListener 录制时长监听
     **/
    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    /**
     * 录制状态监听
     *
     * @param onStatusChangeListener 设置录制状态监听
     **/
    public void setOnStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        this.onStatusChangeListener = onStatusChangeListener;
    }


}
