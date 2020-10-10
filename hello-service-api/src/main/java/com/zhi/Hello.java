package com.zhi;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 发送的消息实体类
 * @Author WenZhiLuo
 * @Date 2020-10-10 10:22
 */
@Data
@AllArgsConstructor
public class Hello implements Serializable {
    private String message;
    private String description;
}
