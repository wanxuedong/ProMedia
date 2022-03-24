package media.share.audiorecoder;

import static android.media.AudioFormat.ENCODING_PCM_16BIT;

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
    private RecordState recordState;

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
    public short channels = 1;

    /**
     * 音频数据的采样率
     **/
    public short sampleRate = 16000;

    /**
     * 音频数据的采样位数，16或8
     **/
    public short audioFormat = 16;

    public AudioRecorder() {
        recordExecutor = Executors.newSingleThreadExecutor();
        String recorderName = System.currentTimeMillis() + "";
        pcmFile = new File("sdcard/AAFileFactory/", recorderName + ".pcm");
        wavFile = new File("sdcard/AAFileFactory/", recorderName + ".wav");
        if (pcmFile.exists()) {
            pcmFile.delete();
        }
        if (wavFile.exists()) {
            wavFile.delete();
        }

        recordState = RecordState.IDLE;
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
        this.channels = (short) (channels == AudioFormat.CHANNEL_IN_MONO ? 2 : 1);
        this.sampleRate = (short) sampleRate;
        this.audioFormat = (short) (audioFormat == ENCODING_PCM_16BIT ? 16 : 8);
        audioRecord = new AudioRecord(audioSource, sampleRate, channels, audioFormat, minBufferSize);
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
            PcmToWavUtil.convert(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath(), channels, sampleRate, audioFormat);
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
