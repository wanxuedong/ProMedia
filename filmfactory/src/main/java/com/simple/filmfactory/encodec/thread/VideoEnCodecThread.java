package com.simple.filmfactory.encodec.thread;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.simple.filmfactory.encodec.BaseMediaEnCoder;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：视频录制线程
 */
public class VideoEnCodecThread extends Thread {
    private WeakReference<BaseMediaEnCoder> videoWeakReference;

    private boolean isExit;

    private MediaCodec videoEncodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferinfo;
    private MediaMuxer mediaMuxer;

    private int videoTrackIndex;
    private long prevOutputPTSUs = 0;


    public VideoEnCodecThread(WeakReference<BaseMediaEnCoder> encoderWeakReference) {
        this.videoWeakReference = encoderWeakReference;
        if (videoWeakReference.get() != null) {
            videoEncodec = videoWeakReference.get().videoEncodec;
            videoFormat = videoWeakReference.get().videoFormat;
            videoBufferinfo = videoWeakReference.get().videoBufferinfo;
            mediaMuxer = videoWeakReference.get().mediaMuxer;
        }
        videoTrackIndex = -1;
    }

    @Override
    public void run() {
        super.run();
        videoTrackIndex = -1;
        isExit = false;
        if (videoWeakReference.get() != null) {
            videoWeakReference.get().videoExit = false;
        }
        videoEncodec.start();
        while (true) {
            if (isExit) {

                videoEncodec.stop();
                videoEncodec.release();
                videoEncodec = null;

                //如果video退出了
                if (videoWeakReference.get() != null) {
                    if (videoWeakReference.get().videoExit) {
                        if (videoWeakReference.get().stopDownLatch != null) {
                            videoWeakReference.get().stopDownLatch.countDown();
                        }
                    }
                }
                break;
            }

            //从输出队列中取出编码操作之后的数据
            int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);

            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                videoTrackIndex = mediaMuxer.addTrack(videoEncodec.getOutputFormat());
                if (videoWeakReference.get() != null) {
                    if (videoWeakReference.get().startDownLatch != null) {
                        videoWeakReference.get().startDownLatch.countDown();
                    }
                    try {
                        //等待音频合成准备好
                        if (videoWeakReference.get().videoDownLatch != null) {
                            videoWeakReference.get().videoDownLatch.await();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                    outputBuffer.position(videoBufferinfo.offset);
                    outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);

                    //设置时间戳
                    videoBufferinfo.presentationTimeUs = getPTSUs();
                    mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, videoBufferinfo);
                    if (videoWeakReference.get() != null) {
                        if (videoWeakReference.get().onMediaInfoListener != null) {
                            videoWeakReference.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                        }
                    }

                    prevOutputPTSUs = videoBufferinfo.presentationTimeUs;
                    videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                }
            }
        }

    }

    /**
     * 获取下一个时间戳
     *
     * @return
     */
    private long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        if (result < prevOutputPTSUs) {
            result += (prevOutputPTSUs - result);
        }
        return result;
    }

    /**
     * 退出录制和合成
     **/
    public void exit() {
        isExit = true;
        if (videoWeakReference.get() != null) {
            videoWeakReference.get().videoExit = true;
        }
    }

}

