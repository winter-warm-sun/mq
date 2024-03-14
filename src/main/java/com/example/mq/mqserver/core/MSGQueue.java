package com.example.mq.mqserver.core;

import com.example.mq.common.ConsumerEnv;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * 这个类表示一个存储消息的队列
 * MSG => Message
 */
@Data
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
    // 当前队列都有哪些消费者订阅了
    private List<ConsumerEnv> consumerEnvList=new ArrayList<>();
    // 记录当前取到了第几个消费者，方便实现轮循策略
    private AtomicInteger consumerSeq=new AtomicInteger(0);

    // 添加一个新的订阅者
    public void addConsumerEnv(ConsumerEnv consumerEnv) {
        consumerEnvList.add(consumerEnv);
    }

    // 订阅者的删除暂时先不考虑
    // 挑选一个订阅者，用来处理当前的消息（按照轮循的方式）
    public ConsumerEnv chooseConsumer() {
        if(consumerEnvList.size()==0) {
            // 该队列没有人订阅的
            return null;
        }
        // 计算一下当前要取的元素的下标
        int index=consumerSeq.get()%consumerEnvList.size();
        consumerSeq.getAndIncrement();
        return consumerEnvList.get(index);
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
