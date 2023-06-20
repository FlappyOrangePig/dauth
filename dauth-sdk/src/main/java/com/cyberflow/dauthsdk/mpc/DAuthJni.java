package com.cyberflow.dauthsdk.mpc;

import com.cyberflow.dauthsdk.mpc.entity.JniOutBuffer;

import java.util.ArrayList;

public final class DAuthJni {

    private static volatile DAuthJni sInstance = null;

    public static DAuthJni getInstance() {
        if (sInstance == null) {
            synchronized (DAuthJni.class) {
                if (sInstance == null) {
                    sInstance = new DAuthJni();
                }
            }
        }
        return sInstance;
    }

    private DAuthJni() {
    }

    public native void init();

    /**
     * 生成多片秘钥
     */
    public native String[] generateSignKeys(int threshold, int nParties, String[] keyIds);

    /**
     * 刷新秘钥
     * @param ids key对应的id
     * @param keys key。注意id和key的数量要一致
     */
    public native String[] refreshKeys(String[] ids, String[] keys);

    /**
     * 本地签名
     * 由签名可以反推出公钥和钱包地址，然后用这个EOA钱包地址通过合约创建AA账户
     */
    public native String localSignMsg(String msgHash, String[] ids, String[] keys);

    /**
     * 签名包含很多步，是个循环，第一步是这个方法
     */
    public native long remoteSignMsg(String msgHash, String localKey, String localId, String[] remoteIds, ArrayList<JniOutBuffer> outBuffer);

    /**
     * 签名包含很多步，是个循环，第二步开始一直用这个方法
     * @param context 签名上下文，填入[remoteSignMsg]方法的返回值
     * @param remoteId 远端秘钥分片索引
     * @param buffer 输入buffer
     * @param outBuffer 输出buffer
     * @return 是否结束，结束时outBuffer将返回签名结果
     */
    public native boolean remoteSignRound(long context, String remoteId, byte[] buffer, ArrayList<JniOutBuffer> outBuffer);

    /**
     * 在remoteSignRound成功时获取签名结果
     */
    public native String getSignature(long context);

    static {
        System.loadLibrary("dauth");
    }
}
