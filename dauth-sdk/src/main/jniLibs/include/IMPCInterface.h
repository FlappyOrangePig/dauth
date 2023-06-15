#pragma once

extern "C"
{
    //生成MPC签名密钥分片, 函数比较耗时，建议采用异步调用
    //threshold: MPC签名门限数
    //nParties: 参与签名的参与方数量
    //retCount: 返回的数组长度
    //使用完后 freeMemory() 进行内存释放
    //eg. const char** keys = generateSignKeys(2, 3, count); freeMemoryArray(keys, count);
    //eg.  test/genMain.cpp

    const char** generateSignKeys( int threshold, int nParties, int& retCount);

    //释放字符串数组内存
    //p: 字符串数组指针
    //count: 字符串数组长度
    void freeMemoryArray(const char** p, int count);

    void freeMemory(const char* p);

    //刷新私钥
    //keys:原始私钥数组
    //count:数组长度
    //return: 新私钥数组，长度与输入长度一致
    //使用完后 freeMemoryArray() 进行内存释放
    const char**  refreshKeys(const char** keys, int count);


    //本地签名一个消息
    //msghash: 需要签名的消息的hash字符串，一定是hash字符串
    //keys: 签名的私钥数组
    //indies: 签名私钥的索引数组，索引顺序与generateSignKeys产生的key数组索引一致
    //keyCount: 私钥数组长度
    const char* localSignMsg(const char* msghash, const char** keys, int* indices, int keyCount );


    //分布式签名
    //msghash: 需要签名的消息的hash字符串，一定是hash字符串
    //outBuffer: 签名过程中输出的buffer地址
    //outLen: 输出的buffer长度
    //输出的buffer内容用网络推送到远端eg.  send(s, outBuffer, outLen)
    //return:  返回一个地址，在签名过程中使用，调用remoteSignRound 时传入该地址

    //--note--- //使用outBuffer之后 调用freeMemory释放内存 freeMemory(*outBuffer);
    typedef struct 
    {
        const char* localKey;//参与联合签名的本地签名的私钥
        int localIndex;//本地签名的私钥的索引，索引顺序与generateSignKeys产生的key数组索引一致
        int* remoteIndices; //参与签名的节点索引数组，索引顺序与generateSignKeys产生的key数组索引一致
        int indexCount;//远程签名的节点数，如果是3签2，该处参与远程签名的节点数为1（排除自己）
    } RemoteSignKeyInfo;
    
    void* remoteSignMsg(const char* msghash, const RemoteSignKeyInfo* signInfo, char** outBuffer , unsigned int& outLen);
    
    //分布式签名过程函数
    //p: remoteSignMsg的输出
    //remoteIndex: 远端的key索引，与generateSignKeys产生的key数组索引一致
    //buffer: 收到的远程数据包地址
    //len: 收到的远程数据包长度
    //outBuffer: 本轮计算的输出内容地址
    //outLen: 本轮计算输出的内容长度
    //returu : false，签名过程还未完成，需要继续执行签名过程，true:签名已经结束，outBuffer为签名结果

     //--note--- //使用outBuffer之后 调用freeMemory释放内存 freeMemory(*outBuffer);
    bool remoteSignRound(void* p, int remoteIndex, const char* buffer,unsigned int len, char** outBuffer , unsigned int& outLen);
}