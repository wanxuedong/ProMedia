package com.simple.filmfactory.encodec.thread;

import com.simple.filmfactory.encodec.BaseMediaEnCoder;
import com.simple.filmfactory.encodec.listener.OnStatusChangeListener;

import java.lang.ref.WeakReference;

/**
 * @author wan
 * 创建日期：2022/08/04
 * 描述：开始和停止合成音视频控制器线程
 */
public class MediaMuxerThread extends Thread{

    private WeakReference<BaseMediaEnCoder> encoderWeakReference;

    public MediaMuxerThread(WeakReference<BaseMediaEnCoder> encoderWeakReference) {
        this.encoderWeakReference = encoderWeakReference;
    }

    @Override
    public void run() {
        super.run();
        startMuxer(encoderWeakReference);
        quitMuxer(encoderWeakReference);
    }

    /**
     * 开始音视频合成
     *
     * @param mediaWeakReference 当前录制合成器
     **/
    private static synchronized void startMuxer(WeakReference<BaseMediaEnCoder> mediaWeakReference) {
        if (mediaWeakReference.get().mediaMuxer == null) {
            return;
        }
        try {
            mediaWeakReference.get().startDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaWeakReference.get().mediaMuxer.start();
        if (mediaWeakReference.get() != null) {
            mediaWeakReference.get().encodeStart = true;
        }
        //音频和视频都准备好，才开始执行合成
        if (mediaWeakReference.get().videoDownLatch != null){
            mediaWeakReference.get().videoDownLatch.countDown();
        }
        if (mediaWeakReference.get().audioDownLatch != null){
            mediaWeakReference.get().audioDownLatch.countDown();
        }

    }

    /**
     * 退出音视频合成
     *
     * @param mediaWeakReference 当前录制合成器
     **/
    private static synchronized void quitMuxer(WeakReference<BaseMediaEnCoder> mediaWeakReference) {
        if (mediaWeakReference.get().mediaMuxer == null) {
            return;
        }
        try {
            if (mediaWeakReference.get().stopDownLatch != null){
                mediaWeakReference.get().stopDownLatch.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaWeakReference.get().mediaMuxer.stop();
        mediaWeakReference.get().mediaMuxer.release();
        mediaWeakReference.get().mediaMuxer = null;

        if (mediaWeakReference.get().onStatusChangeListener != null) {
            mediaWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.END);
        }

    }

}
