package com.example.mq.mqserver.core;

import java.io.Serializable;

public class BasicProperties implements Serializable {
    // 消息的唯一身份标识. 此处为了保证 id 的唯一性, 使用 UUID 来作为 message id
    private String messageId;
    // 是一个消息上带有的内容, 和 bindingKey 做匹配.
    // 如果当前的交换机类型是 DIRECT, 此时 routingKey 就表示要转发的队列名.
    // 如果当前的交换机类型是 FANOUT, 此时 routingKey 无意义(不使用).
    // 如果当前的交换机类型是 TOPIC, 此时 routingKey 就要和 bindingKey 做匹配. 符合要求的才能转发给对应队列.
    private String routingKey;
    // 这个属性表示消息是否要持久化. 1 表示不持久化, 2 表示持久化. (RabbitMQ 就是这样搞的....)
    private int deliverMode = 1;

    // 其实针对 RabbitMQ 来说, BasicProperties 里面还有很多别的属性. 其他的属性暂时先不考虑了.


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public int getDeliverMode() {
        return deliverMode;
    }

    public void setDeliverMode(int deliverMode) {
        this.deliverMode = deliverMode;
    }

    @Override
    public String toString() {
        return "BasicProperties{" +
                "messageId='" + messageId + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", deliverMode=" + deliverMode +
                '}';
    }
}
