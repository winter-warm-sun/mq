package com.example.mq.mqserver.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 这个类表示一个存储消息的队列
 * MSG => Message
 */
public class MSGQueue {
    // 表示队列的身份标识
    private String name;
    // 表示队列是否持久化，true 表示持久化保存，false 表示不持久化
    private boolean durable = false;
    // 这个属性为true，表示这个队列只能被一个消费者使用；如果为false，则是大家都能使用
    // 这个独占功能，也是先把字段列在这里，具体的独占功能暂时先不实现
    private boolean exclusive=false;
    // 为 true 表示没有人使用之后，就自动删除；false 则是不会自动删除
    // 这个自动删除功能，也是先把字段列在这里，具体的独占功能暂时先不实现
    private boolean autoDelete=false;
    // 也是表示扩展参数，当前也是先列在这里，先暂时不实现
    private Map<String,Object> arguments=new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }
}
