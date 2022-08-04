package com.simple.filmfactory.encodec.mediathread;

import android.media.MediaCodec;
import android.media.MediaMuxer;
import android.os.SystemClock;

import com.simple.filmfactory.encodec.WlBaseMediaEncoder;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * @author wan
 * 创建日期：2022/08/05
 * 描述：音频录制线程
 */
public class AudioEnCodecThread extends Thread {
    private WeakReference<WlBaseMediaEncoder> audioWeakReference;
    public boolean isExit;


    private MediaCodec audioEncodec;
    private MediaCodec.BufferInfo audioBufferinfo;
    private MediaMuxer mediaMuxer;

    private int audioTrackIndex;
    private long pts;


    public AudioEnCodecThread(WeakReference<WlBaseMediaEncoder> encoderWeakReference) {
        this.audioWeakReference = encoderWeakReference;
        audioEncodec = audioWeakReference.get().audioEncodec;
        audioBufferinfo = audioWeakReference.get().mAudioBuffInfo;
        mediaMuxer = audioWeakReference.get().mediaMuxer;
        pts = 0;
        audioTrackIndex = -1;
    }


    @Override
    public void run() {
        super.run();
        isExit = false;
        audioWeakReference.get().audioExit = false;
        audioEncodec.start();
        while (true) {
            if (isExit) {
                audioEncodec.stop();
                audioEncodec.release();
                audioEncodec = null;

                //如果audio退出了
                if (audioWeakReference.get().audioExit) {
                    if (audioWeakReference.get().stopDownLatch != null) {
                        audioWeakReference.get().stopDownLatch.countDown();
                    }
                }
                break;
            }

            int outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferinfo, 0);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                audioTrackIndex = mediaMuxer.addTrack(audioEncodec.getOutputFormat());
                if (audioWeakReference.get().startDownLatch != null) {
                    audioWeakReference.get().startDownLatch.countDown();
                }
                try {
                    //等待视频合成准备好
                    if (audioWeakReference.get().audioDownLatch != null) {
                        audioWeakReference.get().audioDownLatch.await();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                while (outputBufferIndex >= 0) {
                    if (!audioWeakReference.get().encodeStart) {
                        SystemClock.sleep(10);
                        continue;
                    }

                    ByteBuffer outputBuffer = audioEncodec.getOutputBuffers()[outputBufferIndex];
                    outputBuffer.position(audioBufferinfo.offset);
                    outputBuffer.limit(audioBufferinfo.offset + audioBufferinfo.size);

                    //设置时间戳
                    if (pts == 0) {
                        pts = audioBufferinfo.presentationTimeUs;
                    }
                    audioBufferinfo.presentationTimeUs = audioBufferinfo.presentationTimeUs - pts;
                    //写入数据
                    mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, audioBufferinfo);

                    audioEncodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferinfo, 0);
                }
            }

        }

    }

    public void exit() {
        isExit = true;
        audioWeakReference.get().audioExit = true;
    }
}
