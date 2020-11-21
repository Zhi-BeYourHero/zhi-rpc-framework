package com.zhi.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-11-21 13:10
 */
public class RpcConstants {

    /**
     * 魔法数 检验 RpcMessage
     * zhi rpc
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'z', (byte) 'r', (byte) 'p', (byte) 'c'};
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    //版本信息
    public static final byte VERSION = 1;
    public static final byte TOTAL_LENGTH = 15;
    //请求
    public static final byte REQUEST_TYPE = 1;
    //响应
    public static final byte RESPONSE_TYPE = 2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final int HEAD_LENGTH = 15;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;
}