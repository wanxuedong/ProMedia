package com.simple.filmfactory.encodec.thread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.util.Log;

import com.simple.filmfactory.encodec.BaseMediaEnCoder;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * @author wan
 * 创建日期：2022/08/05
 * 描述：音频录制线程
 */
public class AudioEnCodecThread extends Thread {
    private WeakReference<BaseMediaEnCoder> audioWeakReference;
    public boolean isExit;

    public static final int SAMPLES_PER_FRAME = 1024;
    public static final int FRAMES_PER_BUFFER = 25;
    private static final int TIMEOUT_USEC = 10000;
    private static final int SAMPLE_RATE = 44100;
    private static final int[] AUDIO_SOURCES = new int[]{MediaRecorder.AudioSource.DEFAULT};

    private MediaCodec audioEncodec;
    private MediaCodec.BufferInfo audioBufferinfo;
    private MediaMuxer mediaMuxer;

    private int audioTrackIndex;

    private AudioRecord audioRecord;
    private long prevOutputPTSUs = 0;

    public AudioEnCodecThread(WeakReference<BaseMediaEnCoder> encoderWeakReference) {
        this.audioWeakReference = encoderWeakReference;
        if (audioWeakReference.get() != null) {
            audioEncodec = audioWeakReference.get().audioEncodec;
            audioBufferinfo = audioWeakReference.get().mAudioBuffInfo;
            mediaMuxer = audioWeakReference.get().mediaMuxer;
        }
        audioTrackIndex = -1;
    }


    @Override
    public void run() {
        super.run();
        isExit = false;
        if (audioWeakReference.get() != null) {
            audioWeakReference.get().audioExit = false;
        }
        audioEncodec.start();
        final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
        int readBytes;
        while (true) {
            if (isExit) {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                    try {
                        //添加延时，防止编码中的数据未完全处理
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
                audioEncodec.stop();
                audioEncodec.release();
                audioEncodec = null;

                //如果audio退出了
                if (audioWeakReference.get() != null) {
                    if (audioWeakReference.get().audioExit) {
                        if (audioWeakReference.get().stopDownLatch != null) {
                            audioWeakReference.get().stopDownLatch.countDown();
                        }
                    }
                }
                break;
            }
            if (audioRecord == null) {
                prepareAudioRecord();
            } else {
                buf.clear();
                readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
                if (readBytes > 0) {
                    buf.position(readBytes);
                    buf.flip();
                    Log.e("ang-->", "解码音频数据:" + readBytes);
                    try {
                        encode(buf, readBytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * 开始解码
     *
     * @param buffer 音频数据
     * @param length 音频长度
     **/
    private void encode(final ByteBuffer buffer, final int length) {
        if (isExit) {
            return;
        }
        final ByteBuffer[] inputBuffers = audioEncodec.getInputBuffers();
        final int inputBufferIndex = audioEncodec.dequeueInputBuffer(TIMEOUT_USEC);
        //向编码器输入数据
        if (inputBufferIndex >= 0) {
            final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            if (buffer != null) {
                inputBuffer.put(buffer);
            }
            if (length <= 0) {
                audioEncodec.queueInputBuffer(inputBufferIndex, 0, 0, getPTSUs(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                audioEncodec.queueInputBuffer(inputBufferIndex, 0, length, getPTSUs(), 0);
            }
        } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
        }

        //读取编码器中的数据，并进行合成
        ByteBuffer[] encoderOutputBuffers = audioEncodec.getOutputBuffers();
        int encoderStatus;
        do {
            encoderStatus = audioEncodec.dequeueOutputBuffer(audioBufferinfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                audioTrackIndex = mediaMuxer.addTrack(audioEncodec.getOutputFormat());
                if (audioWeakReference.get() != null) {
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
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = audioEncodec.getOutputBuffers();
            } else {
                if ((audioBufferinfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    audioBufferinfo.size = 0;
                }

                if (audioBufferinfo.size != 0) {
                    ByteBuffer outputBuffer = encoderOutputBuffers[encoderStatus];
                    outputBuffer.position(audioBufferinfo.offset);
                    outputBuffer.limit(audioBufferinfo.offset + audioBufferinfo.size);
                    //设置时间戳
                    audioBufferinfo.presentationTimeUs = getPTSUs();
                    //写入数据
                    mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, audioBufferinfo);
                    prevOutputPTSUs = audioBufferinfo.presentationTimeUs;
                }

                audioEncodec.releaseOutputBuffer(encoderStatus, false);
            }
        } while ((encoderStatus >= 0));
    }

    /**
     * audioRecord初始化
     **/
    private void prepareAudioRecord() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        try {
            final int min_buffer_size = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);
            int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
            if (buffer_size < min_buffer_size) {
                buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;
            }

            audioRecord = null;
            for (final int source : AUDIO_SOURCES) {
                try {
                    audioRecord = new AudioRecord(source, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
                    if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                        audioRecord = null;
                    }
                } catch (Exception e) {
                    audioRecord = null;
                }
                if (audioRecord != null) {
                    break;
                }
            }
        } catch (final Exception e) {
        }

        if (audioRecord != null) {
            audioRecord.startRecording();
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
        if (audioWeakReference.get() != null) {
            audioWeakReference.get().audioExit = true;
        }
    }
}
