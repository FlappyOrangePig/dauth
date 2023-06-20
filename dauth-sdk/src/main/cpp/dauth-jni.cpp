#include <IMPCInterface.h> // CMake中接口路径已经添加

#include "common.h"
#include "dauth-jni.h"
#include <string>
#include <vector>
#include <memory>
#include <iostream>
using std::string;
using std::vector;

std::string jstringToString(JNIEnv *env, jstring jStr) {
    const char *cStr = env->GetStringUTFChars(jStr, nullptr);
    std::string str(cStr);
    env->ReleaseStringUTFChars(jStr, cStr);
    return str;
}

/**
 * java字符串转c字符串。
 * @param env
 * @param jStrings
 * @return 返回值创建在堆上必须由调用者free
 */
vector<string> jStrings2CStrings(JNIEnv *env, jobjectArray jStrings) {
    jsize arrayLength = env->GetArrayLength(jStrings);
    // Allocate a vector to store the C++ strings
    vector<string> stringVector(arrayLength);
    // Iterate over the Java array and convert each string to a C++ string
    for (int i = 0; i < arrayLength; i++) {
        auto javaString = (jstring) env->GetObjectArrayElement(jStrings, i);
        const char* stringChars = env->GetStringUTFChars(javaString, nullptr);
        stringVector[i] = stringChars;
        env->ReleaseStringUTFChars(javaString, stringChars);
    }
    return stringVector;
}

/**
 * c字符串转java字符串。还干了把mpc.so传来的cStrings释放了的活。
 * @param env
 * @param cStrings
 * @param count
 * @return java字符串
 */
jobjectArray cStrings2JStrings(JNIEnv *env, const char **cStrings, int count) {
    jobjectArray outputArray = env->NewObjectArray(count, env->FindClass("java/lang/String"),
                                                   nullptr);
    //LOGV("cStrings2JStrings >> %d", count)
    for (int i = 0; i < count; i++) {
        const char *str = (cStrings)[i];
        //LOGV("%d) >> [%zu]:%s", i, strlen(str), str)
        jstring jstr = env->NewStringUTF(str);
        env->SetObjectArrayElement(outputArray, i, jstr);
        env->DeleteLocalRef(jstr);
        //LOGV("%d) <<", i)
    }
    freeMemoryArray(cStrings, count);
    //LOGV("cStrings2JStrings <<")
    return outputArray;
}

jobject createJniOutBuffer(JNIEnv* env, const void* outBuffer, unsigned int len, const char* id) {
    // Find the JniOutBuffer class and its constructor
    jclass jniOutBufferClass = env->FindClass("com/cyberflow/dauthsdk/mpc/entity/JniOutBuffer");
    jmethodID jniOutBufferConstructor = env->GetMethodID(jniOutBufferClass, "<init>", "([BLjava/lang/String;)V");
    // Create a new jbyteArray object and copy the contents of the C byte array to it
    jbyteArray jBytes = env->NewByteArray(len);
    env->SetByteArrayRegion(jBytes, 0, len, (jbyte*)outBuffer);
    // Call the constructor to create a new JniOutBuffer instance
    jobject jniOutBuffer = env->NewObject(jniOutBufferClass, jniOutBufferConstructor, jBytes, env->NewStringUTF(id));
    // Return the new instance
    return jniOutBuffer;
}

void putBytesToBytesArrayList(JNIEnv *env, jobject outBuffer, SignOutBuffer *outPtr, int outBufferSize) {
    // 调用反射回传outBuffer
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListAddMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    for (int i = 0; i < outBufferSize; ++i) {
        SignOutBuffer each = outPtr[i];
        auto buffLen = (jsize)each.len;
        // 转jByteArray
        jbyteArray jByteArray = env->NewByteArray(buffLen);
        env->SetByteArrayRegion(jByteArray, 0, buffLen, (const jbyte *) each.outBuffer);
        string id = each.id;

        jobject created = createJniOutBuffer(env, each.outBuffer, each.len, each.id);
        // 调用反射把jByteArray添加到ArrayList
        env->CallBooleanMethod(outBuffer, arrayListAddMethod, created);

        freeMemory(outPtr->id);
        freeMemory((const char *) outPtr->outBuffer);
    }

    // 释放输出buffer
    if (outPtr) {
        freeMemory((const char *) outPtr);
    }
}

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

void Java_com_cyberflow_dauthsdk_mpc_DAuthJni_init(JNIEnv *env, jobject object) {
    std::string hello = "Hello dauth from JNI.";
    LOGD("init:%s", hello.c_str())
}

jobjectArray Java_com_cyberflow_dauthsdk_mpc_DAuthJni_generateSignKeys
        (JNIEnv *env, jobject, jint threshold, jint nParties, jobjectArray keyIds) {
    LOGD("generateSignKeys:threshold=%d,nParties=%d", threshold, nParties)

    int idCount = env->GetArrayLength(keyIds);
    vector<string> ids = jStrings2CStrings(env, keyIds);
    const char **array = new const char *[ids.size()];
    for (size_t i = 0; i < ids.size(); i++) {
        array[i] = ids[i].c_str();
    }

    const char **keys = (generateSignKeys(threshold, array, idCount));

    delete[] array;

    return cStrings2JStrings(env, keys, nParties);
}

jobjectArray Java_com_cyberflow_dauthsdk_mpc_DAuthJni_refreshKeys
        (JNIEnv *env, jobject, jobjectArray ids, jobjectArray keys) {
    int count = env->GetArrayLength(ids);
    LOGD("refreshKeys:count=%d", count)
    vector<string> vecIds = jStrings2CStrings(env, ids);
    vector<string> vecKeys = jStrings2CStrings(env, keys);

    KeyInfo keyInfoArray[count];
    for (int i = 0; i < count; ++i) {
        keyInfoArray[i].id = vecIds[i].c_str();
        keyInfoArray[i].key = vecKeys[i].c_str();
    }
    const char **refreshed = refreshKeys(reinterpret_cast<const KeyInfo **>(&keyInfoArray), count);
    return cStrings2JStrings(env, refreshed, count);
}

jstring Java_com_cyberflow_dauthsdk_mpc_DAuthJni_localSignMsg
        (JNIEnv *env, jobject, jstring msgHash, jobjectArray ids, jobjectArray keys) {
    string strMsgHash = jstringToString(env, msgHash);
    int count = env->GetArrayLength(ids);
    LOGD("localSignMsg:msghash=%s", strMsgHash.c_str())

    vector<string> vecIds = jStrings2CStrings(env, ids);
    vector<string> vecKeys = jStrings2CStrings(env, keys);

    KeyInfo keyInfoArray[count];
    for (int i = 0; i < count; ++i) {
        keyInfoArray[i].id = vecIds[i].c_str();
        keyInfoArray[i].key = vecKeys[i].c_str();
    }
    LOGD("before local sign");
    const char *signResult = localSignMsg(strMsgHash.c_str(), keyInfoArray, count);

    LOGD("localSignMsg:signResult=%s", signResult)

    return env->NewStringUTF(signResult);
}

jlong Java_com_cyberflow_dauthsdk_mpc_DAuthJni_remoteSignMsg
        (JNIEnv *env, jobject, jstring msgHash, jstring localKey, jstring localId,
         jobjectArray remoteIds, jobject outBuffer) {

    string strMsgHash = jstringToString(env, msgHash);
    string strLocalKey = jstringToString(env, localKey);
    string strLocalId = jstringToString(env, localId);
    vector<string> vecRemoteIds = jStrings2CStrings(env, remoteIds);

    LOGD("remoteSignMsg msghash=%s,localId=%s",  strMsgHash.c_str(), strLocalId.c_str())

    const char **remoteIdArray = new const char *[vecRemoteIds.size()];
    for (size_t i = 0; i < vecRemoteIds.size(); i++) {
        remoteIdArray[i] = vecRemoteIds[i].c_str();
    }

    KeyInfo keyInfo;
    keyInfo.id = strLocalId.c_str();
    keyInfo.key = strLocalKey.c_str();
    RemoteSignKeyInfo remoteSignKeyInfo;
    remoteSignKeyInfo.remoteCount = vecRemoteIds.size();
    remoteSignKeyInfo.remotePartyids = remoteIdArray;
    remoteSignKeyInfo.selfKeyInfo = &keyInfo;

    SignOutBuffer* ptrOutBuffer = nullptr;
    unsigned int outBufferSize = 0;
    LOGD("before sign")
    void *context = remoteSignMsg(strMsgHash.c_str(), &remoteSignKeyInfo, &ptrOutBuffer, outBufferSize);
    LOGD("outBufferSize=%d", outBufferSize)
    putBytesToBytesArrayList(env, outBuffer, ptrOutBuffer, outBufferSize);

    // 释放输入id数组
    delete [] remoteIdArray;

    long contextHandle = (jlong) context;
    return contextHandle;
}

jboolean Java_com_cyberflow_dauthsdk_mpc_DAuthJni_remoteSignRound
        (JNIEnv *env, jobject, jlong context, jstring remoteId, jbyteArray buffer,
         jobject outBuffer) {
    void *contextHandler = reinterpret_cast<void *>(context);
    string strRemoteId = jstringToString(env, remoteId);

    unsigned int bufferLen = env->GetArrayLength(buffer);
    jbyte *jBufferBytes = env->GetByteArrayElements(buffer, nullptr);
    const char *cBufferBytes = (const char *) jBufferBytes;

    LOGD("removeSignRound remoteIndex=%s %ld", strRemoteId.c_str(), context)

    SignOutBuffer* ptrOutBuffer = nullptr;
    unsigned int outBufferSize = 0;
    bool finished = remoteSignRound(contextHandler, strRemoteId.c_str(), cBufferBytes, bufferLen, &ptrOutBuffer, outBufferSize);
    LOGD("outBufferSize=%d finish=%d", outBufferSize, finished)
    putBytesToBytesArrayList(env, outBuffer, ptrOutBuffer, outBufferSize);

    // 释放输入buffer
    env->ReleaseByteArrayElements(buffer, jBufferBytes, JNI_ABORT);
    return finished;
}

jstring Java_com_cyberflow_dauthsdk_mpc_DAuthJni_getSignature
        (JNIEnv *env, jobject, jlong context) {
    void *contextHandler = reinterpret_cast<void *>(context);
    const char *signature = getSignature(contextHandler);
    return env->NewStringUTF(signature);
}

#ifdef __cplusplus
}
#endif