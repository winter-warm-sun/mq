package com.example.mq.mqclient;

import com.example.mq.arguments.BasicAckArguments;
import com.example.mq.common.Consumer;
import com.example.mq.returns.BasicReturns;

import java.util.concurrent.ConcurrentHashMap;

public class Channel {
    private String channelId;
    // 当前这个 channel 属于哪个连接
    private Connection connection;
    // 用来存储后续客户端收到的服务器的响应
    private ConcurrentHashMap<String, BasicReturns> basicReturnsMap=new ConcurrentHashMap<>();
    // 如果当前 Channel 订阅了某个队列，就需要在此处记录下对应回调是啥，当该队列的消息返回回来的时候，调用回调
    // 此处约定一个 Channel 只能有一个回调
    private Consumer consumer=null;

    public Channel(String channelId,Connection connection) {
        this.channelId=channelId;
        this.connection=connection;
    }

    // 在这个方法中，和服务器进行交互，告知服务器，此处客户端创建了新的 channel了
    public boolean createChannel() {
        // 对于创建 Channel 操作来说，payload 就是一个 basicArguments 对象
        BasicAckArguments basicAckArguments=new BasicAckArguments();

    }
}
