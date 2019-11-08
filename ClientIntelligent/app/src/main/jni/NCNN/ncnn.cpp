#include <android/bitmap.h>
#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

#include "ncnn/net.h"

static ncnn::UnlockedPoolAllocator g_blob_pool_allocator;
static ncnn::PoolAllocator g_workspace_pool_allocator;

static ncnn::Mat model_param;
static ncnn::Mat model_bin;
static ncnn::Net net;
static char ptr_in[100], ptr_out[100];

char* getCharArrayFromJCharArray(JNIEnv* env, jcharArray jcharArray, int mode) {
    int len = env->GetArrayLength(jcharArray);
    char* ptr = ptr_out;
    if (mode == 0) {
        ptr = ptr_in;
    }
    jchar* jptr = env->GetCharArrayElements(jcharArray, nullptr);
    for (int i = 0; i < len; i++) {
        ptr[i] = (char)jptr[i];
    }
    env->ReleaseCharArrayElements(jcharArray, jptr, 0);
    return ptr;
}



extern "C" {

//JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
//{
//
//    ncnn::create_gpu_instance();
//
//    return JNI_VERSION_1_4;
//}
//
//JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
//{
//
//    ncnn::destroy_gpu_instance();
//}

JNIEXPORT jboolean JNICALL Java_com_example_android_clientintelligent_interpreter_ncnn_NCNNNative_InitModel(
        JNIEnv* env, jclass type, jbyteArray param, jbyteArray bin) {

    // init param
    {
        int len = env->GetArrayLength(param);
        model_param.create(len, (size_t)1u);
        env->GetByteArrayRegion(param, 0, len, (jbyte*)model_param);
        int ret = net.load_param_mem((const char*)model_param);
        __android_log_print(ANDROID_LOG_DEBUG, "NCNN", "load_param %d %d", ret, len);
    }

    // init bin
    {
        int len = env->GetArrayLength(bin);
        model_bin.create(len, (size_t)1u);
        env->GetByteArrayRegion(bin, 0, len, (jbyte*)model_bin);
        int ret = net.load_model((const unsigned char*)model_bin);
        __android_log_print(ANDROID_LOG_DEBUG, "NCNN", "load_model %d %d", ret, len);
    }

    return JNI_TRUE;
}

JNIEXPORT jfloatArray JNICALL Java_com_example_android_clientintelligent_interpreter_ncnn_NCNNNative_Detect(
        JNIEnv* env, jclass type, jobject bitmap, jboolean use_gpu, jint threads, jcharArray jin_node, jcharArray jout_node)
{
    ncnn::Option opt;
    opt.lightmode = true;
    opt.num_threads = threads;
    opt.blob_allocator = &g_blob_pool_allocator;
    opt.workspace_allocator = &g_workspace_pool_allocator;

    // use vulkan compute
//    if (use_gpu == JNI_TRUE) {
//        if (ncnn::get_gpu_count() != 0) {
//            opt.use_vulkan_compute = true;
//        } else {
//            return NULL;
//        }
//    }

    net.opt = opt;


    // ncnn from bitmap
    ncnn::Mat in;
    {
        AndroidBitmapInfo info;
        AndroidBitmap_getInfo(env, bitmap, &info);
        int width = info.width;
        int height = info.height;

        if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
            return nullptr;

        void* indata;
        AndroidBitmap_lockPixels(env, bitmap, &indata);

        in = ncnn::Mat::from_pixels((const unsigned char*)indata, ncnn::Mat::PIXEL_RGBA2BGR, width, height);

        AndroidBitmap_unlockPixels(env, bitmap);
    }

    jfloatArray result;
    {
        const float mean_vals[3] = {104.f, 117.f, 123.f};
        const float normal_vals[3] = {0.017f, 0.017f, 0.017f};
        in.substract_mean_normalize(mean_vals, normal_vals);

        ncnn::Extractor ex = net.create_extractor();

        //ex.set_vulkan_compute(use_gpu);

        char* in_node = getCharArrayFromJCharArray(env, jin_node, 0);
        ex.input(in_node, in);

        ncnn::Mat out;
        char* out_node = getCharArrayFromJCharArray(env, jout_node, 1);
        ex.extract(out_node, out);

        result = env->NewFloatArray(out.w);
        jfloat *destDims = env->GetFloatArrayElements(result, nullptr);
        for (int j = 0; j < out.w; j++) {
            destDims[j] = out[j];
        }
        env->ReleaseFloatArrayElements(result, destDims, 0);
    }

    return result;
}

JNIEXPORT void JNICALL Java_com_example_android_clientintelligent_interpreter_ncnn_NCNNNative_Release
    (JNIEnv* env, jclass type){
    net.clear();
}

}
