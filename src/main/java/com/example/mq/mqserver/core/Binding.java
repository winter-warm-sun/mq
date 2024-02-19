package com.example.mq.mqserver.core;

import lombok.Data;

/*
 * 表示队列和交换机之间的关联关系
 */
@Data
public class Binding {
    private String exchangeName;
    private String queueName;
    // bindingKey 就是在出题, 要求领红包的人要画个 "桌子" 出来~~
    private String bindingKey;

    // Binding 这个东西, 依附于 Exchange 和 Queue 的!!!
    // 比如, 对于持久化来说, 如果 Exchange 和 Queue 任何一个都没有持久化,
    // 此时你针对 Binding 持久化是没有任何意义的

}
