package media.share.audiorecoder;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;

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
 *
 * @author mac
 */
public class AudioRecorder implements Runnable {

    /**
     * 录音器
     **/
    private AudioRecord audioRecord;

    /**
     * 当前播放状态
     **/
    private boolean isRecording;

    /**
     * 录制线程池
     **/
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

    /**
     * 音频数据的声道数，1：单声道，2：双声道
     **/
    public short channels = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 音频数据的采样率
     **/
    public short sampleRate = 16000;

    /**
     * 音频数据的采样位数，16或8
     **/
    public short audioFormat = AudioFormat.ENCODING_PCM_8BIT;

    /**
     * 录制的文件所在路径
     **/
    private String basePath = "sdcard/AAFileFactory/";

    public AudioRecorder() {
        recordExecutor = Executors.newSingleThreadExecutor();
        isRecording = false;
    }

    /**
     * 初始化AudioRecorder
     *
     * @param audioSource 输入源
     * @param channels    声道数
     * @param sampleRate  音频采样率
     * @param audioFormat 编码制式和采样大小, android支持的采样大小16bit 或者8bit。主流的采用16bit
     **/
    @SuppressLint("MissingPermission")
    public void initAudioRecord(int audioSource, int channels, int sampleRate, int audioFormat) {
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channels, audioFormat);
        this.channels = (short) channels;
        this.sampleRate = (short) sampleRate;
        this.audioFormat = (short) audioFormat;
        audioRecord = new AudioRecord(audioSource, sampleRate, channels, audioFormat, minBufferSize);
        String recorderName = (channels == AudioFormat.CHANNEL_IN_MONO ? "单声道" : "双声道") + "_" + sampleRate + "_" +
                (audioFormat == AudioFormat.ENCODING_PCM_8BIT ? "8位" : "16位") + "_" + System.currentTimeMillis();
        pcmFile = new File(basePath, recorderName + ".pcm");
        wavFile = new File(basePath, recorderName + ".wav");
        if (pcmFile.exists()) {
            pcmFile.delete();
        }
        if (wavFile.exists()) {
            wavFile.delete();
        }
    }

    /**
     * 开始录制
     **/
    public void startRecord() {
        if (isRecording) {
            return;
        }
        isRecording = true;
        recordExecutor.execute(this);

    }

    /**
     * 停止录制
     **/
    public void stopRecord() {
        isRecording = false;
        audioRecord.stop();
    }

    @Override
    public void run() {
        audioRecord.startRecording();
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(pcmFile));
            byte[] data = new byte[minBufferSize];
            while (isRecording) {
                int size = audioRecord.read(data, 0, minBufferSize);
                outputStream.write(data, 0, size);
            }
            outputStream.flush();
            outputStream.close();
            PcmCovWavUtil pcmCovWavUtil = new PcmCovWavUtil(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath(), channels, sampleRate, audioFormat);
            pcmCovWavUtil.convertWaveFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
