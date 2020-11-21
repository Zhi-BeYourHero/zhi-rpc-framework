package com.zhi.remoting.dto;

import lombok.*;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-11-21 13:12
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    //消息类型
    private byte messageType;

    //序列化类型
    private byte codec;

    //请求id
    private int requestId;

    //数据内容
    private Object data;
}