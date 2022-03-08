package com.simpo.promusic.player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.simpo.promusic.opengl.WlGLSurfaceView;
import com.simpo.promusic.opengl.WlRender;
import com.simpo.promusic.player.bean.MuteEnum;
import com.simpo.promusic.player.bean.WlTimeInfoBean;
import com.simpo.promusic.player.listener.WlOnCompleteListener;
import com.simpo.promusic.player.listener.WlOnErrorListener;
import com.simpo.promusic.player.listener.WlOnLoadListener;
import com.simpo.promusic.player.listener.WlOnLogListener;
import com.simpo.promusic.player.listener.WlOnPauseResumeListener;
import com.simpo.promusic.player.listener.WlOnPreparedListener;
import com.simpo.promusic.player.listener.WlOnTimeInfoListener;
import com.simpo.promusic.player.listener.WlOnValumeDBListener;
import com.simpo.promusic.player.log.MyLog;
import com.simpo.promusic.player.threadXUtil.AbstractLife;
import com.simpo.promusic.player.threadXUtil.ThreadX;
import com.simpo.promusic.utils.WlVideoSupportUitl;

import java.nio.ByteBuffer;

/**
 * @author wanxuedong
 * 使用ffmpeg+openSlES实现音频的播放，暂停，再播，停止，倍速，展示当前播放和总时间长
 **/
public class MusicPlayer {

    /**
     * 加载FFmpeg音视频处理库
     * **/
    static {
        System.loadLibrary("native-lib");
//        System.loadLibrary("avcodec-57");
//        System.loadLibrary("avdevice-57");
//        System.loadLibrary("avfilter-6");
//        System.loadLibrary("avformat-57");
//        System.loadLibrary("avutil-55");
//        System.loadLibrary("postproc-54");
//        System.loadLibrary("swresample-2");
//        System.loadLibrary("swscale-4");
    }

    /**
     * 日志回调
     **/
    private final int LOG = 10001;
    /**
     * 多媒体读取完毕回调
     **/
    private final int PREPARED = 10002;
    /**
     * 加载中回调
     **/
    private final int LOAD = 10003;
    /**
     * 时间内进度回调
     **/
    private final int TIME_INFO = 10004;
    /**
     * 播放完毕回调
     **/
    private final int COMPLETE = 10005;
    /**
     * 下一个回调
     **/
    private final int ON_NEXT = 10006;
    /**
     * 错误回调
     **/
    private final int ERROR = 10007;
    /**
     * 声音大小回调
     **/
    private final int SOUND = 10008;

    /**
     * 数据源
     **/
    private String source;
    private WlGLSurfaceView wlGLSurfaceView;
    private WlTimeInfoBean wlTimeInfoBean;
    private boolean playNext = false;
    private static int duration = -1;

    private Handler handler;

    public MusicPlayer() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case LOG:
                        if (wlOnLogListener != null) {
                            wlOnLogListener.onLog(msg.arg1, msg.obj.toString());
                        }
                        break;
                    case PREPARED:
                        if (wlOnPreparedListener != null) {
                            wlOnPreparedListener.onPrepared();
                        }
                        break;
                    case LOAD:
                        if (wlOnLoadListener != null) {
                            wlOnLoadListener.onLoad((Boolean) msg.obj);
                        }
                        break;
                    case TIME_INFO:
                        if (wlOnTimeInfoListener != null) {
                            wlTimeInfoBean = (WlTimeInfoBean) msg.obj;
                            wlOnTimeInfoListener.onTimeInfo(wlTimeInfoBean);
                        }
                        break;
                    case COMPLETE:
                        if (wlOnCompleteListener != null) {
                            wlOnCompleteListener.onComplete();
                        }
                        break;
                    case ON_NEXT:
                        if ((Boolean) msg.obj) {
                            playNext = false;
                            prepare();
                        }
                        break;
                    case ERROR:
                        if (wlOnErrorListener != null) {
                            stop();
                            wlOnErrorListener.onError(msg.arg1, (String) msg.obj);
                        }
                        break;
                    case SOUND:
                        if (wlOnValumeDBListener != null) {
                            wlOnValumeDBListener.onDbValue(msg.arg1);
                        }
                        break;
                    default:
                }
            }
        };
    }

    /**
     * 音量大小，0-100
     **/
    private static int volumePercent = 100;
    /**
     * 声道模式，左，右，立体声道，默认立体
     **/
    private static MuteEnum muteEnum = MuteEnum.MUTE_CENTER;
    /**
     * 播放速度，正常为1
     **/
    private static float speed = 1.0f;
    /**
     * 播放音调，正常为1
     **/
    private static float pitch = 1.0f;

    private WlOnLogListener wlOnLogListener;
    private WlOnPreparedListener wlOnPreparedListener;
    private WlOnLoadListener wlOnLoadListener;
    private WlOnPauseResumeListener wlOnPauseResumeListener;
    private WlOnTimeInfoListener wlOnTimeInfoListener;
    private WlOnCompleteListener wlOnCompleteListener;
    private WlOnErrorListener wlOnErrorListener;
    private WlOnValumeDBListener wlOnValumeDBListener;

    private MediaFormat mediaFormat;
    private MediaCodec mediaCodec;
    private Surface surface;
    private MediaCodec.BufferInfo info;

    /**
     * 设置音频数据源
     **/
    public void setSource(String source) {
        this.source = source;
    }

    public void setWlGLSurfaceView(WlGLSurfaceView wlGLSurfaceView) {
        this.wlGLSurfaceView = wlGLSurfaceView;
        wlGLSurfaceView.getWlRender().setOnSurfaceCreateListener(new WlRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(Surface s) {
                if (surface == null) {
                    surface = s;
                    MyLog.d("onSurfaceCreate");
                }
            }
        });
    }

    public void setWlOnLogListener(WlOnLogListener wlOnLogListener) {
        this.wlOnLogListener = wlOnLogListener;
    }

    /**
     * 设置准备状态监听
     **/
    public void setWlOnPreparedListener(WlOnPreparedListener wlOnPreparedListener) {
        this.wlOnPreparedListener = wlOnPreparedListener;
    }

    public void setWlOnLoadListener(WlOnLoadListener wlOnLoadListener) {
        this.wlOnLoadListener = wlOnLoadListener;
    }

    public void setWlOnPauseResumeListener(WlOnPauseResumeListener wlOnPauseResumeListener) {
        this.wlOnPauseResumeListener = wlOnPauseResumeListener;
    }

    public void setWlOnTimeInfoListener(WlOnTimeInfoListener wlOnTimeInfoListener) {
        this.wlOnTimeInfoListener = wlOnTimeInfoListener;
    }

    public void setWlOnCompleteListener(WlOnCompleteListener wlOnCompleteListener) {
        this.wlOnCompleteListener = wlOnCompleteListener;
    }

    public void setWlOnErrorListener(WlOnErrorListener wlOnErrorListener) {
        this.wlOnErrorListener = wlOnErrorListener;
    }

    public void setWlOnValumeDBListener(WlOnValumeDBListener wlOnValumeDBListener) {
        this.wlOnValumeDBListener = wlOnValumeDBListener;
    }

    /**
     * 等待C++层回调即可，表示数据准备完成
     **/
    public void onLogMessage(int type, String message) {
        Message msg = handler.obtainMessage();
        msg.what = LOG;
        msg.arg1 = type;
        msg.obj = message;
        handler.sendMessage(msg);
    }

    /**
     * 等待C++层回调即可，表示数据准备完成
     **/
    public void onCallPrepared() {
        Message msg = handler.obtainMessage();
        msg.what = PREPARED;
        handler.sendMessage(msg);
    }

    /**
     * 数据加载中回调
     **/
    public void onCallLoad(boolean load) {
        Message msg = handler.obtainMessage();
        msg.what = LOAD;
        msg.obj = load;
        handler.sendMessage(msg);
    }

    /**
     * 当前播放时长和总时长回调
     **/
    public void onCallTimeInfo(int currentTime, int totalTime) {
        duration = totalTime;
        if (wlTimeInfoBean == null) {
            wlTimeInfoBean = new WlTimeInfoBean();
        }
        wlTimeInfoBean.setCurrentTime(currentTime);
        wlTimeInfoBean.setTotalTime(totalTime);
        Message msg = handler.obtainMessage();
        msg.what = TIME_INFO;
        msg.obj = wlTimeInfoBean;
        handler.sendMessage(msg);
    }

    /**
     * 音频播放完毕回调
     **/
    public void onCallComplete() {
        stop();
        Message msg = handler.obtainMessage();
        msg.what = COMPLETE;
        handler.sendMessage(msg);
    }

    /**
     * 开始播放下一首的回调
     **/
    public void onCallNext() {
        Message msg = handler.obtainMessage();
        msg.what = ON_NEXT;
        msg.obj = playNext;
        handler.sendMessage(msg);
    }

    /**
     * 错误信息回调
     **/
    public void onCallError(int code, String message) {
        Message msg = handler.obtainMessage();
        msg.what = ERROR;
        msg.arg1 = code;
        msg.obj = message;
        handler.sendMessage(msg);
    }

    /**
     * 声音大小回调
     **/
    public void onCallVolumeDB(int db) {
        Message msg = handler.obtainMessage();
        msg.what = SOUND;
        msg.arg1 = db;
        handler.sendMessage(msg);
    }

    /**
     * 回调yuv数据
     **/
    public void onCallRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v) {
        if (wlGLSurfaceView != null) {
            wlGLSurfaceView.setYUVData(width, height, y, u, v);
        }
    }

    /**
     * 判断是否支持当前格式的硬解码
     **/
    public boolean onCallIsSupportMediaCodec(String ffcodecname) {
        return WlVideoSupportUitl.isSupportCodec(ffcodecname);
    }

    /**
     * 初始化MediaCodec
     *
     * @param codecName
     * @param width
     * @param height
     * @param csd_0
     * @param csd_1
     */
    public void initMediaCodec(String codecName, int width, int height, byte[] csd_0, byte[] csd_1) {
        if (surface != null) {
            try {
                wlGLSurfaceView.getWlRender().setRenderType(WlRender.RENDER_MEDIACODEC);
                String mime = WlVideoSupportUitl.findVideoCodecName(codecName);
                mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
                mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
                mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(csd_0));
                mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(csd_1));
                MyLog.d(mediaFormat.toString());
                mediaCodec = MediaCodec.createDecoderByType(mime);

                info = new MediaCodec.BufferInfo();
                mediaCodec.configure(mediaFormat, surface, null, 0);
                mediaCodec.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (wlOnErrorListener != null) {
                wlOnErrorListener.onError(2001, "surface is null");
            }
        }
    }

    private void releaseMediacodec() {
        if (mediaCodec != null) {
            mediaCodec.flush();
            mediaCodec.stop();
            mediaCodec.release();

            mediaCodec = null;
            mediaFormat = null;
            info = null;
        }
    }

    /**
     * 回调硬解码需要的数据
     *
     * @param datasize 数据大小
     * @param data     数据内容
     */
    public void decodeAVPacket(int datasize, byte[] data) {
        if (surface != null && datasize > 0 && data != null && mediaCodec != null) {
            int intPutBufferIndex = mediaCodec.dequeueInputBuffer(10);
            if (intPutBufferIndex >= 0) {
                ByteBuffer byteBuffer = mediaCodec.getInputBuffers()[intPutBufferIndex];
                byteBuffer.clear();
                byteBuffer.put(data);
                mediaCodec.queueInputBuffer(intPutBufferIndex, 0, datasize, 0, 0);
            }
            int outPutBufferIndex = mediaCodec.dequeueOutputBuffer(info, 10);
            while (outPutBufferIndex >= 0) {
                //处理完成，释放ByteBuffer数据,即将缓冲区返回到编解码器
                mediaCodec.releaseOutputBuffer(outPutBufferIndex, true);
                //输出缓冲区出列，最多阻塞“超时”微秒，返回输出缓冲区索引
                outPutBufferIndex = mediaCodec.dequeueOutputBuffer(info, 10);
            }
        }
    }

    /**
     * 播放器进入准备状态
     **/
    public void prepare() {
        if (TextUtils.isEmpty(source)) {
            MyLog.d("source is empty");
            return;
        }
        onCallLoad(true);
        ThreadX.x().run(new AbstractLife() {
            @Override
            public void run() {
                super.run();
                n_prepared(source);
            }
        });
    }

    /**
     * 开始播放音频源
     **/
    public void start() {
        if (TextUtils.isEmpty(source)) {
            MyLog.d("source is empty");
            return;
        }
        ThreadX.x().run(new AbstractLife() {
            @Override
            public void run() {
                super.run();
                n_start();
            }
        });
    }

    /**
     * 暂停音频源
     **/
    public void pause() {
        n_pause();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(true);
        }
    }

    /**
     * 再次播放音频源
     **/
    public void resume() {
        n_resume();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(false);
        }
    }

    /**
     * 停止播放音频源并释放内存
     **/
    public void stop() {
        wlTimeInfoBean = null;
        duration = 0;
        ThreadX.x().run(new AbstractLife() {
            @Override
            public void run() {
                super.run();
                n_stop();
                releaseMediacodec();
            }
        });
    }

    /**
     * 跳转到指定播放位置,单位秒
     **/
    public void seek(final int secds) {
        ThreadX.x().run(new AbstractLife() {
            @Override
            public void run() {
                super.run();
                n_seek(secds);
            }
        });
    }

    /**
     * 设置播放下一首，并且传入下一首的url
     **/
    public void playNext(String url) {
        source = url;
        playNext = true;
        stop();
    }

    /**
     * 获取当前的播放时间，单位秒
     **/
    public int getDuration() {
        return duration;
    }

    /**
     * 设置音量大小，0-100
     **/
    public void setVolume(int percent) {
        if (percent >= 0 && percent <= 100) {
            volumePercent = percent;
            n_volume(percent);
        }
    }

    /**
     * 获取当前的音量
     **/
    public int getVolumePercent() {
        return volumePercent;
    }

    /**
     * 设置声道模式
     **/
    public void setMute(MuteEnum mute) {
        muteEnum = mute;
        n_mute(mute.getValue());
    }

    /**
     * 设置声调,1为正常
     **/
    public void setPitch(float p) {
        pitch = p;
        n_pitch(pitch);
    }

    /**
     * 设置播放速度，1为正常
     **/
    public void setSpeed(float s) {
        speed = s;
        n_speed(speed);
    }

    /**
     * 文件推流
     **/
    public void updateFile(String path, String rtmpUrl) {
        n_update_file(path, rtmpUrl);
    }


    private native void n_prepared(String source);

    private native void n_update_file(String name, String url);

    private native void n_start();

    private native void n_pause();

    private native void n_resume();

    private native void n_stop();

    private native void n_seek(int secds);

    private native void n_volume(int percent);

    private native void n_mute(int mute);

    private native void n_pitch(float pitch);

    private native void n_speed(float speed);

}
