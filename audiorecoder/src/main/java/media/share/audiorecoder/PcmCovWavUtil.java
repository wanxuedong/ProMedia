package media.share.audiorecoder;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PcmCovWavUtil {

    /**
     * 录音的采样频率
     **/
    private int audioRate = 16000;

    /**
     * 录音的声道，单声道
     **/
    private int audioChannel = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 采样位数
     **/
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 缓存的大小
     **/
    private int bufferSize = AudioRecord.getMinBufferSize(audioRate, audioChannel, audioFormat);

    /**
     * pcm文件目录
     **/
    private String pcmPath;

    /**
     * wav文件目录
     **/
    private String wavPath;

    /**
     * 初始化
     *
     * @param pcmPath      PCM文件路径
     * @param wavPath      wav文件路径
     * @param audioChannel 通道数
     * @param audioRate    采样率
     * @param audioFormat  采样位数
     **/
    public PcmCovWavUtil(String pcmPath, String wavPath, int audioChannel, int audioRate, int audioFormat) {
        this.pcmPath = pcmPath;
        this.wavPath = wavPath;
        this.audioChannel = audioChannel;
        this.audioRate = audioRate;
        this.audioFormat = audioFormat;
        this.bufferSize = AudioRecord.getMinBufferSize(audioRate, audioChannel, audioFormat);
    }

    /**
     * 转换函数
     **/
    public void convertWaveFile() {
        File pcmFile = new File(pcmPath);
        if (!pcmFile.exists()) {
            return;
        }
        File wavFile = new File(wavPath);
        if (wavFile.exists()) {
            wavFile.delete();
        }

        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = audioRate;
        int channels = 1;
        long byteRate = 16 * audioRate * channels / 8;
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
            byteRate = 16 * audioRate * channels / 8;
        } else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
            byteRate = 8 * audioRate * channels / 8;
        }

        byte[] data = new byte[bufferSize];
        try {
            in = new FileInputStream(pcmPath);
            out = new FileOutputStream(wavPath);
            totalAudioLen = in.getChannel().size();
            //由于不包括前面的8个字节RIFF和WAV
            totalDataLen = totalAudioLen + 36;
            addWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加wav头部信息
     **/
    private void addWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, long byteRate) throws IOException {
        byte[] header = new byte[44];
        // RIFF 头表示
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        //数据大小，数据大小，真正大小是添加了8bit
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //wave格式
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //fmt Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        int Channel = 1;
        if (audioChannel == AudioFormat.CHANNEL_IN_MONO) {
            Channel = 1;
        } else {
            Channel = 2;
        }
        header[22] = (byte) Channel;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
            header[32] = (byte) (Channel * 16 / 8);
        } else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
            header[32] = (byte) (Channel * 8 / 8);
        }
        header[33] = 0;
        //每个样本的数据位数
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
            header[34] = 16;
        } else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
            header[34] = 8;
        }

        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}

