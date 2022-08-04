package com.simple.filmfactory.encodec.mediathread;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.simple.filmfactory.encodec.WlBaseMediaEncoder;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：视频录制线程
 */
public class VideoEnCodecThread extends Thread {
    private WeakReference<WlBaseMediaEncoder> videoWeakReference;

    private boolean isExit;

    private MediaCodec videoEncodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferinfo;
    private MediaMuxer mediaMuxer;

    private int videoTrackIndex;
    private long pts;


    public VideoEnCodecThread(WeakReference<WlBaseMediaEncoder> encoderWeakReference) {
        this.videoWeakReference = encoderWeakReference;
        videoEncodec = videoWeakReference.get().videoEncodec;
        videoFormat = videoWeakReference.get().videoFormat;
        videoBufferinfo = videoWeakReference.get().videoBufferinfo;
        mediaMuxer = videoWeakReference.get().mediaMuxer;
    }

    @Override
    public void run() {
        super.run();
        pts = 0;
        videoTrackIndex = -1;
        isExit = false;
        videoWeakReference.get().videoExit = false;
        videoEncodec.start();
        while (true) {
            if (isExit) {

                videoEncodec.stop();
                videoEncodec.release();
                videoEncodec = null;

                //如果video退出了
                if (videoWeakReference.get().videoExit) {
                    if (videoWeakReference.get().stopDownLatch != null) {
                        videoWeakReference.get().stopDownLatch.countDown();
                    }
                }
                break;
            }

            //从输出队列中取出编码操作之后的数据
            int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);

            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                videoTrackIndex = mediaMuxer.addTrack(videoEncodec.getOutputFormat());
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
                    if (videoWeakReference.get().onMediaInfoListener != null) {
                        videoWeakReference.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                    }

                    videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                }
            }
        }

    }

    public void exit() {
        isExit = true;
        videoWeakReference.get().videoExit = true;
    }

}

