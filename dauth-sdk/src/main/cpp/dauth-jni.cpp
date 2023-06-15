#include <string>
#include <IMPCInterface.h> // CMake中接口路径已经添加

#include "common.h"
#include "dauth-jni.h"

#ifdef __cplusplus
extern "C" {
#endif

JavaVM *g_vm = nullptr;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_vm = vm;
    JNIEnv *env = nullptr;
    jint result = -1;

    if (g_vm) {
        LOGD("VM init success");
    } else {
        LOGD("VM init failed");
    }

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return result;
    }
    return JNI_VERSION_1_6;
}

/**
 * c字符串转java字符串
 * @param env
 * @param cStrings
 * @param count
 * @return java字符串
 */
jobjectArray cStrings2JStrings(JNIEnv *env, const char **cStrings, int count) {
    jobjectArray outputArray = env->NewObjectArray(count, env->FindClass("java/lang/String"),
                                                   nullptr);
    LOGV("cStrings2JStrings >>")
    for (int i = 0; i < count; i++) {
        const char *str = (cStrings)[i];
        LOGV("%d) >> [%zu]:%s", i, strlen(str), str)
        jstring jstr = env->NewStringUTF(str);
        env->SetObjectArrayElement(outputArray, i, jstr);
        env->DeleteLocalRef(jstr);
        LOGV("%d) <<", i)
    }
    freeMemoryArray(cStrings, count);
    LOGV("cStrings2JStrings <<")
    return outputArray;
}

/**
 * java字符串转c字符串。
 * @param env
 * @param jStrings
 * @return 返回值创建在堆上必须由调用者free
 */
const char **jStrings2CStrings(JNIEnv *env, jobjectArray jStrings) {
    int count = env->GetArrayLength(jStrings);
    const char **cStrings = (const char **) malloc(count * sizeof(const char *));
    LOGV("jStrings2CStrings >>")
    for (int i = 0; i < count; i++) {
        LOGV("%d) >>", i)
        jstring jstr = (jstring) env->GetObjectArrayElement(jStrings, i);
        const char *cstr = env->GetStringUTFChars(jstr, nullptr);
        cStrings[i] = cstr;
        env->DeleteLocalRef(jstr);
        LOGV("%d) <<", i)
    }
    LOGV("jStrings2CStrings <<")
    return cStrings;
}

void putBytesToBytesArrayList(JNIEnv *env, jobject outBuffer, char *outPtr, int outBufferSize) {
    // 调用反射回传outBuffer
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListAddMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    // 转jByteArray
    jbyteArray jByteArray = env->NewByteArray(outBufferSize);
    env->SetByteArrayRegion(jByteArray, 0, outBufferSize, (const jbyte *) outPtr);
    // 调用反射把jByteArray添加到ArrayList
    env->CallBooleanMethod(outBuffer, arrayListAddMethod, jByteArray);
}

void Java_com_cyberflow_dauthsdk_mpc_DAuthJni_init(JNIEnv *env, jobject object) {
    std::string hello = "Hello dauth from JNI.";
    LOGD("init:%s", hello.c_str())
}

jobjectArray Java_com_cyberflow_dauthsdk_mpc_DAuthJni_generateSignKeys
        (JNIEnv *env, jobject, jint threshold, jint nParties) {
    LOGD("generateSignKeys:threshold=%d,nParties=%d", threshold, nParties)
    int retCount = 0;
    const char **keys = (generateSignKeys(threshold, nParties, retCount));
    return cStrings2JStrings(env, keys, nParties);
}

jobjectArray Java_com_cyberflow_dauthsdk_mpc_DAuthJni_refreshKeys
        (JNIEnv *env, jobject, jobjectArray keys, jint count) {
    LOGD("refreshKeys:count=%d", count)

    const char **output = jStrings2CStrings(env, keys);
    const char **refreshed = refreshKeys(output, count);

    free(output);

    return cStrings2JStrings(env, refreshed, count);
}

jstring Java_com_cyberflow_dauthsdk_mpc_DAuthJni_localSignMsg
        (JNIEnv *env, jobject, jstring msghash, jobjectArray keys, jintArray indices) {
    const char *msgHashInC = env->GetStringUTFChars(msghash, nullptr);
    LOGD("localSignMsg:msghash=%s", msgHashInC)

    int indicesCount = env->GetArrayLength(indices);

    jint *indicesInJ = env->GetIntArrayElements(indices, nullptr);
    for (int i = 0; i < indicesCount; i++) {
        LOGD("indices%d)=%d", i, indicesInJ[i]);
    }

    int keyCount = env->GetArrayLength(keys);
    const char **keysInC = jStrings2CStrings(env, keys);
    for (int i = 0; i < keyCount; i++) {
        LOGD("key%d)=%s", i, keysInC[i]);
    }

    const char *signResult = localSignMsg(msgHashInC, keysInC, indicesInJ, indicesCount);
    free(keysInC);
    LOGD("localSignMsg:signResult=%s", signResult)

    return env->NewStringUTF(signResult);
}

jlong Java_com_cyberflow_dauthsdk_mpc_DAuthJni_remoteSignMsg
        (JNIEnv *env, jobject, jstring msghash, jstring localKey, jint localIndex,
         jintArray remoteIndices, jobject outBuffer) {
    const char *msgHashInC = env->GetStringUTFChars(msghash, nullptr);
    int indicesCount = env->GetArrayLength(remoteIndices);
    jint *indicesInJ = env->GetIntArrayElements(remoteIndices, nullptr);
    const char *localKeyInC = env->GetStringUTFChars(localKey, nullptr);
    int localIndexInC = (int) localIndex;

    LOGD("remoteSignMsg msghash=%s,localIndex=%d,remoteIndices=%d",  msgHashInC, localIndexInC, indicesInJ[0])

    RemoteSignKeyInfo keyInfo;
    keyInfo.localKey = localKeyInC;
    keyInfo.localIndex = localIndexInC;
    keyInfo.remoteIndices = indicesInJ;
    keyInfo.indexCount = indicesCount;

    char *outPtr = nullptr;
    unsigned int outBufferSize = 0;
    void *context = remoteSignMsg(msgHashInC, &keyInfo, &outPtr, outBufferSize);
    LOGD("outBufferSize=%d", outBufferSize)
    putBytesToBytesArrayList(env, outBuffer, outPtr, outBufferSize);
    free(outPtr);

    long contextHandle = (jlong) context;
    return contextHandle;
}

jboolean Java_com_cyberflow_dauthsdk_mpc_DAuthJni_remoteSignRound
        (JNIEnv *env, jobject, jlong context, jint remoteIndex, jbyteArray buffer,
         jobject outBuffer) {
    void *contextHandler = reinterpret_cast<void *>(context);
    int cRemoteIndex = remoteIndex;
    unsigned int bufferLen = env->GetArrayLength(buffer);
    jbyte *jBufferBytes = env->GetByteArrayElements(buffer, nullptr);
    const char *cBufferBytes = (const char *) jBufferBytes;

    LOGD("removeSignRound remoteIndex=%d %ld", remoteIndex, context)

    char *outPtr = nullptr;
    unsigned int outBufferSize = 0;
    bool finished = remoteSignRound(contextHandler, cRemoteIndex, cBufferBytes, bufferLen, &outPtr, outBufferSize);
    LOGD("outBufferSize=%d finish=%d", outBufferSize, finished)
    putBytesToBytesArrayList(env, outBuffer, outPtr, outBufferSize);
    free(outPtr);

    return finished;
}

#ifdef __cplusplus
}
#endif