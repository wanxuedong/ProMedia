//
// Created by mac on 2020/9/15.
//

#include "RtmpUpdate.h"
#include "AndroidLog.h"
#include <iostream>

using namespace std;
//引入头文件
extern "C"
{
#include "libavformat/avformat.h"
//引入时间
#include "libavutil/time.h"
}
//引入库
#pragma comment(lib, "avformat.lib")
//工具库，包括获取错误信息等
#pragma comment(lib, "avutil.lib")
//编解码的库
#pragma comment(lib, "avcodec.lib")

int avError(int errNum);

static double r2d(AVRational r) {
    return r.num == 0 || r.den == 0 ? 0. : (double) r.num / (double) r.den;
}

int RtmpUpdate::upDateFile(const char *name, const char *rtmpUrl) {
    //所有代码执行之前要调用av_register_all和avformat_network_init
    //初始化所有的封装和解封装 flv mp4 mp3 mov。不包含编码和解码
    LOGE("two");
    av_register_all();

    //初始化网络库
    avformat_network_init();

    //使用的相对路径，执行文件在bin目录下。test.mp4放到bin目录下即可
    const char *inUrl = name;
    //输出的地址
    const char *outUrl = rtmpUrl;

    //////////////////////////////////////////////////////////////////
    //                   输入流处理部分
    /////////////////////////////////////////////////////////////////
    //打开文件，解封装 avformat_open_input
    //AVFormatContext **ps  输入封装的上下文。包含所有的格式内容和所有的IO。如果是文件就是文件IO，网络就对应网络IO
    //const char *url  路径
    //AVInputFormt * fmt 封装器
    //AVDictionary ** options 参数设置
    AVFormatContext *ictx = NULL;

    //打开文件，解封文件头
    int ret = avformat_open_input(&ictx, inUrl, 0, NULL);
    if (ret < 0) {
        LOGE("three");
        return avError(ret);
    }
    cout << "avformat_open_input success!" << endl;
    //获取音频视频的信息 .h264 flv 没有头信息
    ret = avformat_find_stream_info(ictx, 0);
    if (ret != 0) {
        return avError(ret);
    }
    //打印视频视频信息
    //0打印所有  inUrl 打印时候显示，
    av_dump_format(ictx, 0, inUrl, 0);

    //////////////////////////////////////////////////////////////////
    //                   输出流处理部分
    /////////////////////////////////////////////////////////////////
    AVFormatContext *octx = NULL;
    //如果是输入文件 flv可以不传，可以从文件中判断。如果是流则必须传
    //创建输出上下文
    ret = avformat_alloc_output_context2(&octx, NULL, "flv", outUrl);
    if (ret < 0) {
        return avError(ret);
    }
    cout << "avformat_alloc_output_context2 success!" << endl;
    //配置输出流
    //AVIOcontext *pb  //IO上下文
    //AVStream **streams  指针数组，存放多个输出流  视频音频字幕流
    //int nb_streams;
    //duration ,bit_rate

    //AVStream
    //AVRational time_base
    //AVCodecParameters *codecpar 音视频参数
    //AVCodecContext *codec
    //遍历输入的AVStream
    for (int i = 0; i < ictx->nb_streams; i++) {
        //创建一个新的流到octx中
        AVStream *out = avformat_new_stream(octx, ictx->streams[i]->codec->codec);
        if (!out) {
            return avError(0);
        }
        //复制配置信息 用于mp4 过时的方法
        //ret=avcodec_copy_context(out->codec, ictx->streams[i]->codec);
        ret = avcodec_parameters_copy(out->codecpar, ictx->streams[i]->codecpar);
        if (ret < 0) {
            return avError(ret);
        }
        out->codec->codec_tag = 0;
    }
    av_dump_format(octx, 0, outUrl, 1);

    //////////////////////////////////////////////////////////////////
    //                   准备推流
    /////////////////////////////////////////////////////////////////

    //打开IO
    ret = avio_open(&octx->pb, outUrl, AVIO_FLAG_WRITE);
    if (ret < 0) {
        avError(ret);
    }

    //写入头部信息
    ret = avformat_write_header(octx, 0);
    if (ret < 0) {
        avError(ret);
    }
    cout << "avformat_write_header Success!" << endl;

    //推流每一帧数据
    //int64_t pts  [ pts*(num/den)  第几秒显示]
    //int64_t dts  解码时间 [P帧(相对于上一帧的变化) I帧(关键帧，完整的数据) B帧(上一帧和下一帧的变化)]  有了B帧压缩率更高。
    //uint8_t *data
    //int size
    //int stream_index
    //int flag
    AVPacket avPacket;
    //获取当前的时间戳  微妙
    long long startTime = av_gettime();
    while (true) {
        ret = av_read_frame(ictx, &avPacket);
        if (ret < 0) {
            break;
        }
        cout << avPacket.pts << " " << flush;
        //计算转换时间戳 pts dts
        //获取时间基数
        AVRational itime = ictx->streams[avPacket.stream_index]->time_base;
        AVRational otime = octx->streams[avPacket.stream_index]->time_base;
        avPacket.pts = av_rescale_q_rnd(avPacket.pts, itime, otime,
                                        (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_NEAR_INF));
        avPacket.dts = av_rescale_q_rnd(avPacket.pts, itime, otime,
                                        (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_NEAR_INF));
        //到这一帧时候经历了多长时间
        avPacket.duration = av_rescale_q_rnd(avPacket.duration, itime, otime,
                                             (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_NEAR_INF));
        avPacket.pos = -1;
        //视频帧推送速度
        if (ictx->streams[avPacket.stream_index]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            AVRational tb = ictx->streams[avPacket.stream_index]->time_base;
            //已经过去的时间
            long long now = av_gettime() - startTime;
            long long dts = 0;
            dts = avPacket.dts * (1000 * 1000 * r2d(tb));
            if (dts > now)
                av_usleep(dts - now);
            else {
                cout << "sss";
            }
        }
        //推送  会自动释放空间 不需要调用av_packet_unref
        ret = av_interleaved_write_frame(octx, &avPacket);
        if (ret < 0) {
            break;
        }
        //视频帧推送速度
        //if (avPacket.stream_index == 0)
        //  av_usleep(30 * 1000);
        //释放空间。内部指向的视频空间和音频空间
        //av_packet_unref(&avPacket);
    }
    return 0;
}

int avError(int errNum) {
    char buf[1024];
    //获取错误信息
    av_strerror(errNum, buf, sizeof(buf));
    cout << " failed! " << buf << endl;
    return -1;
}
