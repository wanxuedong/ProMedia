package media.share.audiorecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author wan
 * 创建日期：2022/03/24
 * 描述：WAV头部
 */
public class WaveHeader {

    public char fileID[] = {'R', 'I', 'F', 'F'};

    /**
     * 整个文件的长度减去ID和Size的长度
     **/
    public int fileLength;

    public char wavTag[] = {'W', 'A', 'V', 'E'};

    public char fmtHdrID[] = {'f', 'm', 't', ' '};

    /**
     * 该区块数据的长度，通常16不用变
     **/
    public int fmtHdrLeth = 16;

    /**
     * Data区块存储的音频数据的格式，PCM音频数据的值为1
     **/
    public short formatTag = 1;

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
    public short bitsPerSample = 16;

    /**
     * 每个采样所需的字节数
     **/
    public short blockAlign = (short) (channels * bitsPerSample / 8);

    /**
     * 每个采样存储的bit数
     **/
    public int avgBytesPerSec = blockAlign * sampleRate;


    private char dataHdrID[] = {'d', 'a', 't', 'a'};

    /**
     * 音频数据的长度
     **/
    public int dataHdrLeth;


    public WaveHeader(int fileLength) {
        this.fileLength = fileLength + (44 - 8);
        dataHdrLeth = fileLength;
    }

    /**
     * 初始化WAV头部部分参数
     *
     * @param fileLength    文件大小
     * @param channels      通道数
     * @param sampleRate    采样率
     * @param bitsPerSample 采样位数
     **/
    public WaveHeader(int fileLength, short channels, short sampleRate, short bitsPerSample) {
        this.fileLength = fileLength + (44 - 8);
        dataHdrLeth = fileLength;
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        blockAlign = (short) (channels * bitsPerSample / 8);
        avgBytesPerSec = blockAlign * sampleRate;

    }

    /**
     * 获取WAV格式头
     **/
    public byte[] getHeader() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //RIFF区块

        //以'RIFF'为标识
        WriteChar(bos, fileID);
        //Size是整个文件的长度减去ID和Size的长度
        WriteInt(bos, fileLength);
        //Type是WAVE表示后面需要两个子块：Format区块和Data区块
        WriteChar(bos, wavTag);

        //FORMAT区块

        //以'fmt '为标识
        WriteChar(bos, fmtHdrID);
        //Size表示该区块数据的长度（不包含ID和Size的长度）
        WriteInt(bos, fmtHdrLeth);
        //AudioFormat表示Data区块存储的音频数据的格式，PCM音频数据的值为1
        WriteShort(bos, formatTag);
        //NumChannels表示音频数据的声道数，1：单声道，2：双声道
        WriteShort(bos, channels);
        //SampleRate表示音频数据的采样率
        WriteInt(bos, sampleRate);
        //ByteRate每秒数据字节数 = SampleRate * NumChannels * BitsPerSample / 8
        WriteInt(bos, avgBytesPerSec);
        //BlockAlign每个采样所需的字节数 = NumChannels * BitsPerSample / 8
        WriteShort(bos, blockAlign);
        //BitsPerSample每个采样存储的bit数，8：8bit，16：16bit，32：32bit
        WriteShort(bos, bitsPerSample);

        //DATA区块

        //以'data'为标识
        WriteChar(bos, dataHdrID);
        //Size表示音频数据的长度，N = ByteRate * seconds
        WriteInt(bos, dataHdrLeth);
        bos.flush();
        byte[] r = bos.toByteArray();
        bos.close();
        return r;

    }


    private void WriteShort(ByteArrayOutputStream bos, int s)
            throws IOException {
        byte[] mybyte = new byte[2];
        mybyte[1] = (byte) ((s << 16) >> 24);
        mybyte[0] = (byte) ((s << 24) >> 24);
        bos.write(mybyte);
    }


    private void WriteInt(ByteArrayOutputStream bos, int n) throws IOException {
        byte[] buf = new byte[4];
        buf[3] = (byte) (n >> 24);
        buf[2] = (byte) ((n << 8) >> 24);
        buf[1] = (byte) ((n << 16) >> 24);
        buf[0] = (byte) ((n << 24) >> 24);
        bos.write(buf);

    }


    private void WriteChar(ByteArrayOutputStream bos, char[] id) {
        for (int i = 0; i < id.length; i++) {
            char c = id[i];
            bos.write(c);
        }

    }

}
