package com.example.mq.mqserver.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/*
 * 这个类表示一个存储消息的队列
 * MSG => Message
 */
public class MSGQueue {
    // 表示队列的身份标识.
    private String name;
    // 表示队列是否持久化, true 表示持久化保存, false 表示不持久化.
    private boolean durable = false;
    // 这个属性为 true, 表示这个队列只能被一个消费者使用(别人用不了). 如果为 false 则是大家都能使用
    // 这个 独占 功能, 也是先把字段列在这里, 具体的独占功能暂时先不实现.
    private boolean exclusive = false;
    // 为 true 表示没有人使用之后, 就自动删除. false 则是不会自动删除.
    // 这个 自动删除 功能, 也是先把字段列在这里, 具体的独占功能暂时先不实现.
    private boolean autoDelete = false;
    // 也是表示扩展参数. 当前也是先列在这里, 先暂时不实现
    private Map<String, Object> arguments = new HashMap<>();



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

    public String getArguments() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(arguments);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    public void setArguments(String argumentsJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.arguments = objectMapper.readValue(argumentsJson, new TypeReference<HashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public Object getArguments(String key) {
        return arguments.get(key);
    }

    public void setArguments(String key, Object value) {
        arguments.put(key, value);
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}
