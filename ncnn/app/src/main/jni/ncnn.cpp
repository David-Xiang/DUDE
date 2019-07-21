#include <android/bitmap.h>
#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

// ncnn
#include "ncnn/net.h"

static ncnn::UnlockedPoolAllocator g_blob_pool_allocator;
static ncnn::PoolAllocator g_workspace_pool_allocator;

static ncnn::Mat ncnn_param;
static ncnn::Mat ncnn_bin;
static ncnn::Net ncnn_net;

extern "C" {

// JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
// {
//     __android_log_print(ANDROID_LOG_DEBUG, "NCNN", "JNI_OnLoad");

//     ncnn::create_gpu_instance();

//     return JNI_VERSION_1_4;
// }

// JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
// {
//     __android_log_print(ANDROID_LOG_DEBUG, "NCNN", "JNI_OnUnload");

//     ncnn::destroy_gpu_instance();
// }

// public native boolean Init(byte[] param, byte[] bin, byte[] words);
JNIEXPORT jboolean JNICALL Java_com_example_android_ncnn_NCNN_Init(JNIEnv* env, jobject thiz, jbyteArray param, jbyteArray bin)
{
    ncnn::Option opt;
    opt.lightmode = true;
    opt.num_threads = 4;
    opt.blob_allocator = &g_blob_pool_allocator;
    opt.workspace_allocator = &g_workspace_pool_allocator;

    // use vulkan compute
    // if (ncnn::get_gpu_count() != 0)
    //     opt.use_vulkan_compute = true;

    ncnn_net.opt = opt;

    // init param
    {
        int len = env->GetArrayLength(param);
        ncnn_param.create(len, (size_t)1u);
        env->GetByteArrayRegion(param, 0, len, (jbyte*)ncnn_param);
        int ret = ncnn_net.load_param((const unsigned char*)ncnn_param);
        __android_log_print(ANDROID_LOG_DEBUG, "NCNN", "load_param %d %d", ret, len);
    }

    // init bin
    {
        int len = env->GetArrayLength(bin);
        ncnn_bin.create(len, (size_t)1u);
        env->GetByteArrayRegion(bin, 0, len, (jbyte*)ncnn_bin);
        int ret = ncnn_net.load_model((const unsigned char*)ncnn_bin);
        __android_log_print(ANDROID_LOG_DEBUG, "NCNN", "load_model %d %d", ret, len);
    }

    return JNI_TRUE;
}

// public native String Detect(Bitmap bitmap, boolean use_gpu);
JNIEXPORT void JNICALL Java_com_example_android_ncnn_NCNN_Detect(JNIEnv* env, jobject thiz, jobject bitmap, jboolean use_gpu, jint index)
{
    // if (use_gpu == JNI_TRUE && ncnn::get_gpu_count() == 0)
    // {
    //     return env->NewStringUTF("no vulkan capable gpu");
    // }

    // ncnn from bitmap
    ncnn::Mat in;
    {
        AndroidBitmapInfo info;
        AndroidBitmap_getInfo(env, bitmap, &info);
        int width = info.width;
        int height = info.height;
        if (width != 28 || height != 28)
            return;

        void* indata;
        AndroidBitmap_lockPixels(env, bitmap, &indata);
        in = ncnn::Mat::from_pixels((const unsigned char*)indata, ncnn::Mat::PIXEL_GRAY, 28, 28);
        AndroidBitmap_unlockPixels(env, bitmap);
    }

    // inference
    std::vector<float> cls_scores;
    {
        ncnn::Extractor ex = ncnn_net.create_extractor();

        // ex.set_vulkan_compute(use_gpu);
        ex.set_light_mode(true);

        //__android_log_print(ANDROID_LOG_DEBUG, "NCNN", "before input() called");
        ex.input(0, in);
        //__android_log_print(ANDROID_LOG_DEBUG, "NCNN", "after input() called");

        ncnn::Mat out;
        //__android_log_print(ANDROID_LOG_DEBUG, "NCNN", "before extract() called");
        ex.extract(index, out);
        //__android_log_print(ANDROID_LOG_DEBUG, "NCNN", "after extract() called");

        cls_scores.resize(out.w);
        for (int j=0; j<out.w; j++)
        {
            cls_scores[j] = out[j];
        }
    }

    // return top class
    int top_class = 0;
    float max_score = 0.f;
    for (size_t i=0; i<cls_scores.size(); i++)
    {
        float s = cls_scores[i];
        if (s > max_score)
        {
            top_class = i;
            max_score = s;
        }
    }

    return;
}

}
