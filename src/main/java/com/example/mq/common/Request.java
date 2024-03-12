package com.example.mq.common;

import lombok.Data;

/*
 * 表示一个网络通信中的请求对象，按照自定义协议的格式来展开的
 */
@Data
public class Request {
    private int type;
    private int length;
    private byte[] payload;
}
