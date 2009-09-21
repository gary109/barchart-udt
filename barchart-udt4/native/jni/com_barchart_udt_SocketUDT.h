/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_barchart_udt_SocketUDT */

#ifndef _Included_com_barchart_udt_SocketUDT
#define _Included_com_barchart_udt_SocketUDT
#ifdef __cplusplus
extern "C" {
#endif
/* Inaccessible static: log */
#undef com_barchart_udt_SocketUDT_INFINITE_TTL
#define com_barchart_udt_SocketUDT_INFINITE_TTL -1L
#undef com_barchart_udt_SocketUDT_INFINITE_TIMEOUT
#define com_barchart_udt_SocketUDT_INFINITE_TIMEOUT -1L
#undef com_barchart_udt_SocketUDT_UNLIMITED_BW
#define com_barchart_udt_SocketUDT_UNLIMITED_BW -1i64
#undef com_barchart_udt_SocketUDT_DEFAULT_ACCEPT_QUEUE_SIZE
#define com_barchart_udt_SocketUDT_DEFAULT_ACCEPT_QUEUE_SIZE 256L
#undef com_barchart_udt_SocketUDT_DEFAULT_MAX_SELECTOR_SIZE
#define com_barchart_udt_SocketUDT_DEFAULT_MAX_SELECTOR_SIZE 1024L
#undef com_barchart_udt_SocketUDT_DEFAULT_CONNECTOR_POOL_SIZE
#define com_barchart_udt_SocketUDT_DEFAULT_CONNECTOR_POOL_SIZE 16L
#undef com_barchart_udt_SocketUDT_DEFAULT_MIN_SELECTOR_TIMEOUT
#define com_barchart_udt_SocketUDT_DEFAULT_MIN_SELECTOR_TIMEOUT 10L
#undef com_barchart_udt_SocketUDT_UDT_READ_INDEX
#define com_barchart_udt_SocketUDT_UDT_READ_INDEX 0L
#undef com_barchart_udt_SocketUDT_UDT_WRITE_INDEX
#define com_barchart_udt_SocketUDT_UDT_WRITE_INDEX 1L
#undef com_barchart_udt_SocketUDT_UDT_EXCEPT_INDEX
#define com_barchart_udt_SocketUDT_UDT_EXCEPT_INDEX 2L
#undef com_barchart_udt_SocketUDT_UDT_SIZE_COUNT
#define com_barchart_udt_SocketUDT_UDT_SIZE_COUNT 3L
/* Inaccessible static: _00024assertionsDisabled */
/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    initClass0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_initClass0
  (JNIEnv *, jclass);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    stopClass0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_stopClass0
  (JNIEnv *, jclass);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    initInstance0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_initInstance0
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    initInstance1
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_initInstance1
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    accept0
 * Signature: ()Lcom/barchart/udt/SocketUDT;
 */
JNIEXPORT jobject JNICALL Java_com_barchart_udt_SocketUDT_accept0
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    bind0
 * Signature: (Ljava/net/InetSocketAddress;)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_bind0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    close0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_close0
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    connect0
 * Signature: (Ljava/net/InetSocketAddress;)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_connect0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    hasLoadedRemoteSocketAddress
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_barchart_udt_SocketUDT_hasLoadedRemoteSocketAddress
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    hasLoadedLocalSocketAddress
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_barchart_udt_SocketUDT_hasLoadedLocalSocketAddress
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    getOption0
 * Signature: (ILjava/lang/Class;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_barchart_udt_SocketUDT_getOption0
  (JNIEnv *, jobject, jint, jclass);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    setOption0
 * Signature: (ILjava/lang/Class;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_setOption0
  (JNIEnv *, jobject, jint, jclass, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    listen0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_listen0
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    receive0
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_receive0
  (JNIEnv *, jobject, jint, jint, jbyteArray);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    receive1
 * Signature: (II[BII)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_receive1
  (JNIEnv *, jobject, jint, jint, jbyteArray, jint, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    receive2
 * Signature: (IILjava/nio/ByteBuffer;II)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_receive2
  (JNIEnv *, jobject, jint, jint, jobject, jint, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    select0
 * Signature: ([I[I[I[IJ)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_select0
  (JNIEnv *, jclass, jintArray, jintArray, jintArray, jintArray, jlong);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    selectEx0
 * Signature: ([I[I[I[IJ)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_selectEx0
  (JNIEnv *, jclass, jintArray, jintArray, jintArray, jintArray, jlong);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    send0
 * Signature: (IIIZ[B)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_send0
  (JNIEnv *, jobject, jint, jint, jint, jboolean, jbyteArray);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    send1
 * Signature: (IIIZ[BII)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_send1
  (JNIEnv *, jobject, jint, jint, jint, jboolean, jbyteArray, jint, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    send2
 * Signature: (IIIZLjava/nio/ByteBuffer;II)I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_send2
  (JNIEnv *, jobject, jint, jint, jint, jboolean, jobject, jint, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    updateMonitor0
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_updateMonitor0
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    getErrorCode0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_getErrorCode0
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    getErrorMessage0
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_barchart_udt_SocketUDT_getErrorMessage0
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    clearError0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_clearError0
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    isOpen0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_barchart_udt_SocketUDT_isOpen0
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testEmptyCall0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testEmptyCall0
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testIterateArray0
 * Signature: ([Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testIterateArray0
  (JNIEnv *, jobject, jobjectArray);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testIterateSet0
 * Signature: (Ljava/util/Set;)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testIterateSet0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testMakeArray0
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_com_barchart_udt_SocketUDT_testMakeArray0
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testGetSetArray0
 * Signature: ([IZ)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testGetSetArray0
  (JNIEnv *, jobject, jintArray, jboolean);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testInvalidClose0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testInvalidClose0
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testCrashJVM0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testCrashJVM0
  (JNIEnv *, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testDirectBufferAccess0
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testDirectBufferAccess0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testFillArray0
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testFillArray0
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     com_barchart_udt_SocketUDT
 * Method:    testFillBuffer0
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testFillBuffer0
  (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
