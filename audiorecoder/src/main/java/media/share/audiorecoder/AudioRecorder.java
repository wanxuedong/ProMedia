package media.share.audiorecoder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioRecord;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 音频录制控制类
 **/
public class AudioRecorder implements Runnable {

    private static final String TAG = "CFF";

    /**
     * 录音器
     **/
    private AudioRecord audioRecord;

    /**
     * 当前播放状态
     **/
    private RecordState recordState;
    private Executor recordExecutor;

    /**
     * 录制数据的缓冲区大小
     **/
    private int minBufferSize;

    /**
     * 录制完毕后得到的pcm格式文件
     **/
    private File pcmFile;
    /**
     * 录制完毕后得到的wav格式文件
     **/
    private File wavFile;

    public AudioRecorder(Context context) {
        recordExecutor = Executors.newSingleThreadExecutor();
        pcmFile = new File(context.getExternalFilesDir(""), "demo.pcm");
        wavFile = new File(context.getExternalFilesDir(""), "demo.wav");

        recordState = RecordState.IDLE;
    }

    /**
     * 初始化AudioRecorder
     *
     * @param audioSource 输入源
     * @param sampleRate  音频采样率
     * @param channel     声道数
     * @param audioFormat 编码制式和采样大小, android支持的采样大小16bit 或者8bit。主流的采用16bit
     **/
    @SuppressLint("MissingPermission")
    public void initAudioRecord(int audioSource, int sampleRate, int channel, int audioFormat) {
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat);
        audioRecord = new AudioRecord(audioSource, sampleRate, channel, audioFormat, minBufferSize);
    }

    /**
     * 开始录制
     **/
    public void startRecord() {
        if (recordState != RecordState.IDLE) {
            return;
        }
        recordState = RecordState.RECORDING;
        recordExecutor.execute(this);

    }

    /**
     * 停止录制
     **/
    public void stopRecord() {
        recordState = RecordState.IDLE;
        audioRecord.stop();
    }

    @Override
    public void run() {
        audioRecord.startRecording();
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(pcmFile));
            byte[] data = new byte[minBufferSize];
            while (recordState == RecordState.RECORDING) {
                int size = audioRecord.read(data, 0, minBufferSize);
                outputStream.write(data, 0, size);
            }
            outputStream.flush();
            outputStream.close();
//            PcmToWav.makePcmFileToWavFile(mPcmFile.getAbsolutePath(), mWavFile.getAbsolutePath(), false);
            Log.i(TAG, "录制完成");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    enum RecordState {
        IDLE,
        RECORDING
    }
}
