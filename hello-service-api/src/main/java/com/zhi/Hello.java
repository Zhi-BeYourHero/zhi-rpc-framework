package com.zhi;

import lombok.*;

import java.io.Serializable;

/**
 * @Description 发送的消息实体类
 * @Author WenZhiLuo
 * @Date 2020-10-10 10:22
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {
    private String message;
    private String description;
}
