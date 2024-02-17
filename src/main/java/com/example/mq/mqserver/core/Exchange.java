package com.example.mq.mqserver.core;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/*
* 这个类表示一个交换机
 */
@Data
public class Exchange {
    // 此处使用 name 来作为交换机的身份标识 （唯一的）
    private String name;
    // 交换机类型, DIRECT, FANOUT, TOPIC
    private ExchangeType type=ExchangeType.DIRECT;
    // 该交换机是否要持久化存储，true 表示需要持久化；false 表示不必持久化
    private boolean durable=false;
    // 如果当前交换机，没人使用了，就会自动被删除
    // 这个属性暂时先列在这里，后续的代码中并没有真的实现这个自动删除功能（RabbitMQ是有的）
    private boolean autoDelete=false;
    // arguments 表示的是创建交换机时指定的一些额外的参数选项，后续代码中并没有真的实现对应的功能, 先列出来. (RabbitMQ 也是有的)
    // 为了把这个 arguments 存到数据库中, 就需要把 Map 转成 json 格式的字符串.
    private Map<String,Object> arguments=new HashMap<>();
}
