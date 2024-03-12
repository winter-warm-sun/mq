package com.example.mq.common;

import lombok.Data;

/*
 * 这个对象表示一个响应，也是根据自定义应用层协议来的
 */
@Data
public class Response {
    private int type;
    private int length;
    private byte[] payload;
}
