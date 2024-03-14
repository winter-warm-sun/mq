package com.example.mq.common;

import lombok.Data;

/*
 * 表示一个消费者（完整的执行环境）
 */
@Data
public class ConsumerEnv {
    private String consumerTag;
    private String queueName;
    private boolean autoAck;
    // 通过这个回调来处理收到的消息
    private Consumer consumer;

    public ConsumerEnv(String consumerTag,String queueName,boolean autoAck,Consumer consumer) {
        this.consumerTag=consumerTag;
        this.queueName=queueName;
        this.autoAck=autoAck;
        this.consumer=consumer;
    }
}
