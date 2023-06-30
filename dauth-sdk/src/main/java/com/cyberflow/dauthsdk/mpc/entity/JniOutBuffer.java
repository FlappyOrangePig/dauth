package com.cyberflow.dauthsdk.mpc.entity;

import androidx.annotation.Keep;

@Keep
public class JniOutBuffer {
    private final byte[] bytes;
    private final String id;

    public JniOutBuffer(byte[] bytes, String id) {
        this.bytes = bytes;
        this.id = id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getId() {
        return id;
    }
}
