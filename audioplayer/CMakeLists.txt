cmake_minimum_required(VERSION 3.4.1)

include_directories(src/main/cpp/ffmepg)
include_directories(src/main/cpp/soundtouch/include)
include_directories(src/main/cpp/soundtouch/SoundTouch)

add_library(
        audio-lib

        SHARED

        src/main/cpp/Audio-lib.cpp
        src/main/cpp/FFmpegPlay.cpp
        src/main/cpp/AudioCallJava.cpp
        src/main/cpp/AudioPlayState.cpp
        src/main/cpp/AudioQueue.cpp
        src/main/cpp/OpenSLESPlay.cpp

        src/main/cpp/soundtouch/SoundTouch/AAFilter.cpp
        src/main/cpp/soundtouch/SoundTouch/FIFOSampleBuffer.cpp
        src/main/cpp/soundtouch/SoundTouch/FIRFilter.cpp
        src/main/cpp/soundtouch/SoundTouch/cpu_detect_x86.cpp
        src/main/cpp/soundtouch/SoundTouch/sse_optimized.cpp
        src/main/cpp/soundtouch/SoundTouch/RateTransposer.cpp
        src/main/cpp/soundtouch/SoundTouch/SoundTouch.cpp
        src/main/cpp/soundtouch/SoundTouch/InterpolateCubic.cpp
        src/main/cpp/soundtouch/SoundTouch/InterpolateLinear.cpp
        src/main/cpp/soundtouch/SoundTouch/InterpolateShannon.cpp
        src/main/cpp/soundtouch/SoundTouch/TDStretch.cpp
        src/main/cpp/soundtouch/SoundTouch/PeakFinder.cpp
)

add_library(avcodec-57 SHARED IMPORTED)
set_target_properties(avcodec-57
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavcodec-57.so)


add_library(avdevice-57 SHARED IMPORTED)
set_target_properties(avdevice-57
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavdevice-57.so)


add_library(avfilter-6 SHARED IMPORTED)
set_target_properties(avfilter-6
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavfilter-6.so)


add_library(avformat-57 SHARED IMPORTED)
set_target_properties(avformat-57
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavformat-57.so)


add_library(avutil-55 SHARED IMPORTED)
set_target_properties(avutil-55
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavutil-55.so)

add_library(postproc-54 SHARED IMPORTED)
set_target_properties(postproc-54
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libpostproc-54.so)

add_library(swresample-2 SHARED IMPORTED)
set_target_properties(swresample-2
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libswresample-2.so)


add_library(swscale-4 SHARED IMPORTED)
set_target_properties(swscale-4
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libswscale-4.so)

target_link_libraries(
        audio-lib

        avcodec-57
        avdevice-57
        avfilter-6
        avformat-57
        avutil-55
        postproc-54
        swresample-2
        swscale-4

        OpenSLES

        log
)