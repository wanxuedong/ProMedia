package media.share.audiorecoder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author wan
 * 创建日期：2022/03/24
 * 描述：备注
 */
public class PcmToWavUtil {

    /**
     * pcm格式数据转wav格式数据
     *
     * @param pcmPath       PCM文件路径
     * @param wavPath       wav文件路径
     * @param channels      通道数
     * @param sampleRate    采样率
     * @param bitsPerSample 采样位数
     **/
    public static void convert(String pcmPath, String wavPath, short channels, short sampleRate, short bitsPerSample) throws IOException {
        FileInputStream fis = new FileInputStream(pcmPath);
        FileOutputStream fos = new FileOutputStream(wavPath);

        //计算长度
        int PCMSize = 0;
        byte[] buf = new byte[1024 * 4];
        int size = fis.read(buf);

        while (size != -1) {
            PCMSize += size;
            size = fis.read(buf);
        }
        fis.close();

        //填入参数，比特率等等
        WaveHeader header = new WaveHeader(100, channels, sampleRate, bitsPerSample);

        byte[] h = header.getHeader();
        //长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = PCMSize + (44 - 8);
        header.fmtHdrLeth = 16;
        header.bitsPerSample = bitsPerSample;
        header.sampleRate = sampleRate;
        header.formatTag = 0x0001;
        header.blockAlign = (short) (header.channels * header.bitsPerSample / 8);
        header.avgBytesPerSec = header.blockAlign * header.sampleRate;
        header.dataHdrLeth = PCMSize;

        //WAV标准，头部应该是44字节
        assert h.length == 44;
        //写入wav头部
        fos.write(h, 0, h.length);
        fis = new FileInputStream(pcmPath);
        size = fis.read(buf);
        //写入data音频数据
        while (size != -1) {
            fos.write(buf, 0, size);
            size = fis.read(buf);
        }
        fis.close();
        fos.close();
    }

}
