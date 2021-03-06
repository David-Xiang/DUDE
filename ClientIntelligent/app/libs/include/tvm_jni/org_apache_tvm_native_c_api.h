/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_apache_tvm_LibInfo */

#ifndef _Included_org_apache_tvm_LibInfo
#define _Included_org_apache_tvm_LibInfo
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    nativeLibInit
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_nativeLibInit
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    shutdown
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_shutdown
  (JNIEnv *, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmGetLastError
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_apache_tvm_LibInfo_tvmGetLastError
  (JNIEnv *, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncPushArgLong
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_apache_tvm_LibInfo_tvmFuncPushArgLong
  (JNIEnv *, jobject, jlong);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncPushArgDouble
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_org_apache_tvm_LibInfo_tvmFuncPushArgDouble
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncPushArgString
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_apache_tvm_LibInfo_tvmFuncPushArgString
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncPushArgBytes
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_org_apache_tvm_LibInfo_tvmFuncPushArgBytes
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncPushArgHandle
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_org_apache_tvm_LibInfo_tvmFuncPushArgHandle
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncListGlobalNames
 * Signature: (Ljava/util/List;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmFuncListGlobalNames
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncFree
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmFuncFree
  (JNIEnv *, jobject, jlong);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncGetGlobal
 * Signature: (Ljava/lang/String;Lorg/apache/tvm/Base/RefLong;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmFuncGetGlobal
  (JNIEnv *, jobject, jstring, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncCall
 * Signature: (JLorg/apache/tvm/Base/RefTVMValue;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmFuncCall
  (JNIEnv *, jobject, jlong, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncCreateFromCFunc
 * Signature: (Lorg/apache/tvm/Function/Callback;Lorg/apache/tvm/Base/RefLong;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmFuncCreateFromCFunc
  (JNIEnv *, jobject, jobject, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmFuncRegisterGlobal
 * Signature: (Ljava/lang/String;JI)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmFuncRegisterGlobal
  (JNIEnv *, jobject, jstring, jlong, jint);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmModFree
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmModFree
  (JNIEnv *, jobject, jlong);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmModGetFunction
 * Signature: (JLjava/lang/String;ILorg/apache/tvm/Base/RefLong;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmModGetFunction
  (JNIEnv *, jobject, jlong, jstring, jint, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmModImport
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmModImport
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmArrayFree
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmArrayFree
  (JNIEnv *, jobject, jlong);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmArrayAlloc
 * Signature: ([JIIIIILorg/apache/tvm/Base/RefLong;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmArrayAlloc
  (JNIEnv *, jobject, jlongArray, jint, jint, jint, jint, jint, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmArrayGetShape
 * Signature: (JLjava/util/List;)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmArrayGetShape
  (JNIEnv *, jobject, jlong, jobject);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmArrayCopyFromTo
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmArrayCopyFromTo
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmArrayCopyFromJArray
 * Signature: ([BJJ)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmArrayCopyFromJArray
  (JNIEnv *, jobject, jbyteArray, jlong, jlong);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmArrayCopyToJArray
 * Signature: (J[B)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmArrayCopyToJArray
  (JNIEnv *, jobject, jlong, jbyteArray);

/*
 * Class:     org_apache_tvm_LibInfo
 * Method:    tvmSynchronize
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_apache_tvm_LibInfo_tvmSynchronize
  (JNIEnv *, jobject, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
