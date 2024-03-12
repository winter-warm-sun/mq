package com.example.mq.returns;

import lombok.Data;

import java.io.Serializable;

/*
 * 这个类表示各个远程调用的方法的返回值的公共信息
 */
@Data
public class BasicReturns implements Serializable {
    // 用来标识唯一的请求和响应
    protected String rid;
    // 用来标识一个 channel
    protected String channelId;
    // 表示当前这个远程调用方法的返回值
    protected boolean ok;
}
