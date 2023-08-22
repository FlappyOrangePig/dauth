#pragma once

extern "C"
{

    enum MPCType{
        e_gg18,
        e_gg20,
        e_cmp
    };
    //生成MPC签名密钥分片, 函数比较耗时，建议采用异步调用
    //threshold: MPC签名门限数
    //partyIds: 参与签名的参与方的id
    //使用完后 freeMemory() 进行内存释放
    //eg. const char** partyids = ["id1", "id2", "id3"]
    //eg. const char** keys = generateSignKeys(2, partyids, 3); freeMemoryArray(keys, 3);
    //eg.  test/genMain.cpp

    const char** generateSignKeys(int threshold, const char** partyIds, int idCount, MPCType type =  MPCType::e_gg18);

    //释放字符串数组内存
    //p: 字符串数组指针
    //count: 字符串数组长度
    void freeMemoryArray(const char** p, int count);

    void freeMemory(const char* p);

    typedef struct
    {
        const char* id;
        const char* key;
        /* data */
    }KeyInfo;
    

    //刷新私钥
    //keys:原始私钥数组
    //count:数组长度
    //return: 新私钥数组，长度与输入长度一致
    //使用完后 freeMemoryArray() 进行内存释放
    const char**  refreshKeys(const KeyInfo** keys, int count, MPCType type =  MPCType::e_gg18);


    //本地签名一个消息
    //msghash: 需要签名的消息的hash字符串，一定是hash字符串
    //keys: 签名的私钥数组
    //ids: 签名私钥的id，与generateSignKeys产生key的id一致
    //keyCount: 私钥数组长度
    
    const char* localSignMsg(const char* msghash, const KeyInfo* keys, int keyCount, MPCType type =  MPCType::e_gg20 );


    //分布式签名
    //msghash: 需要签名的消息的hash字符串，一定是hash字符串
    //outBuffer: 签名过程中输出的buffer地址
    //outLen: 输出的buffer长度
    //输出的buffer内容用网络推送到远端eg.  send(s, outBuffer, outLen)
    //return:  返回一个地址，在签名过程中使用，调用remoteSignRound 时传入该地址

    //--note--- //outSign ,循环删除SignOutBuffer内部的outBuffer和id，freeMemory(outBuffer),freeMemory(id);
    //再调用freeMemory释放outSign内存 freeMemory(outSign);
    typedef struct 
    {
        KeyInfo* selfKeyInfo; //本地签名的私钥
        const char** remotePartyids; //参与签名的节点ID数组，由生成key时的ID决定
        int remoteCount;//远程签名的节点数，
    } RemoteSignKeyInfo;

    typedef struct{
        const char* id;//输出数据需要发送到远端的id;
        void* outBuffer;//需要发送的数据指针
        unsigned int len;//数据长度
    } SignOutBuffer;
    
    void* remoteSignMsg(const char* msghash, const RemoteSignKeyInfo* signInfo, SignOutBuffer** outSign , unsigned int& outBufferCount, MPCType type =  MPCType::e_gg20);
    
    //分布式签名过程函数
    //p: remoteSignMsg的输出
    //remoteId: 远端的id
    //buffer: 收到的远程数据包地址
    //len: 收到的远程数据包长度
    //outBuffer: 本轮计算的输出内容地址
    //outLen: 本轮计算输出的内容长度
    //returu : false，签名过程还未完成，需要继续执行签名过程，true:签名已经结束，调用getSignature 获取签名结果

    //--note--- //outSign ,循环删除SignOutBuffer内部的outBuffer和id，freeMemory(outBuffer),freeMemory(id);
    //再调用freeMemory释放outSign内存 freeMemory(outSign);
    bool remoteSignRound(void* p, const char* remoteId, const char* buffer,unsigned int len, SignOutBuffer** outSign , unsigned int& outLen);


    //获取签名结果
    //当remoteSignRound返回true时调用该函数获取签名结果
    const char* getSignature(void* p);
}

