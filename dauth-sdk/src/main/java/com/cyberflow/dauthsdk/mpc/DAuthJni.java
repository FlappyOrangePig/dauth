package com.cyberflow.dauthsdk.mpc;

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
    public native String[] generateSignKeys(int threshold, int nParties);

    /**
     * 刷新秘钥
     */
    public native String[] refreshKeys(String[] keys, int count);

    /**
     * 本地签名
     * 由签名可以反推出公钥和钱包地址，然后用这个EOA钱包地址通过合约创建AA账户
     */
    public native String localSignMsg(String msghash, String[] keys, int[] indices);

    /**
     * 签名包含很多步，是个循环，第一步是这个方法
     * @param msghash 输入消息的hash
     * @param localKey 本地分片
     * @param localIndex 分片索引
     * @param remoteIndices 远端分片数组
     * @param outBuffer 输出参数
     * @return 签名上下文句柄
     */
    public native long remoteSignMsg(String msghash, String localKey, int localIndex, int[] remoteIndices, ArrayList<byte[]> outBuffer);

    /**
     * 签名包含很多步，是个循环，第二步开始一直用这个方法
     * @param context 签名上下文，填入[remoteSignMsg]方法的返回值
     * @param remoteIndex 远端秘钥分片索引
     * @param buffer 输入buffer
     * @param outBuffer 输出buffer
     * @return 是否结束，结束时outBuffer将返回签名结果
     */
    public native boolean remoteSignRound(long context, int remoteIndex, byte[] buffer, ArrayList<byte[]> outBuffer);

    static {
        System.loadLibrary("dauth");
    }
}
