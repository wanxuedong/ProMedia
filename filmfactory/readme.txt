

MediaCodeC参考：https://www.jianshu.com/p/f5a1c9318524


MediaMuxer使用注意点

1.一定要设置时间戳bufferInfo.presentationTimeUs，否则音视频不同步
2.MediaMuxer 的 start() 和 release() 只能调用一次，而编码的时候又必须将音频和视频两个格式轨道addTrack进MediaMuxer 之后，
才能调用start()，结束的时候也需要音视频都结束编码才能release() 。
3.视频可以通过mediaCodec.signalEndOfInputStream()结束录制，但音频需要mediaCodec.queueInputBuffer传入结束标志BUFFER_FLAG_END_OF_STREAM