#include <jni.h>
#include <string>
#include <caffe2/predictor/predictor.h>
#include <caffe2/core/operator.h>
#include <caffe2/core/timer.h>

#include "caffe2/core/init.h"

#define PROTOBUF_USE_DLLS 1
#define CAFFE2_USE_LITE_PROTO 1

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#define IMG_H 28
#define IMG_W 28
#define IMG_C 1
#define MAX_DATA_SIZE IMG_H * IMG_W * IMG_C
#define alog(...) __android_log_print(ANDROID_LOG_ERROR, "Caffe2", __VA_ARGS__);

static caffe2::NetDef _initNet, _predictNet;
static caffe2::Predictor *_predictor;
static char raw_data[MAX_DATA_SIZE];
static float input_data[MAX_DATA_SIZE];
static caffe2::Workspace ws;
static caffe2::TensorCPU input;

// A function to load the NetDefs from protobufs.
void loadToNetDef(AAssetManager* mgr, caffe2::NetDef* net, const char *filename) {
    AAsset* asset = AAssetManager_open(mgr, filename, AASSET_MODE_BUFFER);
    assert(asset != nullptr);
    const void *data = AAsset_getBuffer(asset);
    assert(data != nullptr);
    off_t len = AAsset_getLength(asset);
    assert(len != 0);
    if (!net->ParseFromArray(data, len)) {
        alog("[loadToNetDef] Couldn't parse net from data.\n");
    }
    AAsset_close(asset);
}

char* jstringToChar(JNIEnv* env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}


extern "C"
void Java_com_ufo_aicamera_MainActivity_initCaffe2(
        JNIEnv* env,
        jobject /* this */,
        jobject assetManager,
        jstring initNetPath,
        jstring predictNetPath) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);

    char* cInitNetPath = jstringToChar(env, initNetPath);
    char* cPredictNetPath = jstringToChar(env, predictNetPath);

    loadToNetDef(mgr, &_initNet, cInitNetPath);
    loadToNetDef(mgr, &_predictNet, cPredictNetPath);
    free(cInitNetPath);
    free(cPredictNetPath);
    input = caffe2::Tensor(1,caffe2::DeviceType::CPU);
    input.Resize(std::vector<int>({1, IMG_C, IMG_H, IMG_W}));
    _predictor = new caffe2::Predictor(_initNet, _predictNet);
}

extern "C"
jstring
Java_com_ufo_aicamera_MainActivity_toCppString(
        JNIEnv* env,
        jobject /* this */,
        jstring msg) {
    return env->NewStringUTF(jstringToChar(env, msg));
}

float avg_fps = 0.0;
float total_fps = 0.0;
int iters_fps = 10;

extern "C"
JNIEXPORT void JNICALL
Java_com_ufo_aicamera_MainActivity_classificationFromCaffe2(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray img) {
    if (!_predictor) {
        return;
    }
    jsize img_len = env->GetArrayLength(img);
    jbyte * Y_data = env->GetByteArrayElements(img, 0);

    memcpy(input.mutable_data<float>(), input_data, IMG_H * IMG_W * IMG_C * sizeof(float));
    caffe2::Predictor::TensorList input_vec{input};
    caffe2::Predictor::TensorList output_vec;
    caffe2::Timer t;
    //t.Start();
    _predictor->operator()(input_vec, &output_vec);
    //float fps = 1000/t.MilliSeconds();
    //total_fps += fps;
    //avg_fps = total_fps / iters_fps;
    //total_fps -= avg_fps;

    //std::ostringstream stringStream;
    //stringStream << avg_fps << " FPS\n";
    //return env->NewStringUTF(stringStream.str().c_str());
    return;
}


