package com.example.mq.mqserver.core;

import lombok.Data;

/*
 * 表示队列和交换机之间的关联关系
 */
@Data
public class Binding {
    private String exchangeName;
    private String queueName;
    // bindingKey：队列上的字符串，用于和 Message 的routingKey 做匹配
    private String bindingKey;

    // Binding是依附于 Exchange 和Queue 的
    // 比如，对于持久化来说，如果 Exchange 和Queue 任何一个都没有持久化
    // 此时针对 Binding 持久化是没有任何意义的

}
