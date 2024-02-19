package com.example.mq.mqserver.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/*
 * 这个类表示一个交换机
 */
public class Exchange {
    // 此处使用 name 来作为交换机的身份标识. (唯一的)
    private String name;
    // 交换机类型, DIRECT, FANOUT, TOPIC
    private ExchangeType type = ExchangeType.DIRECT;
    // 该交换机是否要持久化存储. true 表示需要持久化; false 表示不必持久化.
    private boolean durable = false;
    // 如果当前交换机, 没人使用了, 就会自动被删除.
    // 这个属性暂时先列在这里, 后续的代码中并没有真的实现这个自动删除功能~~ (RabbitMQ 是有的)
    private boolean autoDelete = false;
    // arguments 表示的是创建交换机时指定的一些额外的参数选项. 后续代码中并没有真的实现对应的功能, 先列出来. (RabbitMQ 也是有的)
    // 为了把这个 arguments 存到数据库中, 就需要把 Map 转成 json 格式的字符串.
    private Map<String, Object> arguments = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExchangeType getType() {
        return type;
    }

    public void setType(ExchangeType type) {
        this.type = type;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    // 这里的 get set 用于和数据库交互使用.
    public String getArguments() {
        // 是把当前的 arguments 参数, 从 Map 转成 String (JSON)
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(arguments);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 如果代码真异常了, 返回一个空的 json 字符串就 ok
        return "{}";
    }

    // 这个方法, 是从数据库读数据之后, 构造 Exchange 对象, 会自动调用到
    public void setArguments(String argumentsJson) {
        // 把参数中的 argumentsJson 按照 JSON 格式解析, 转成
        // 上述的 Map 对象
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.arguments = objectMapper.readValue(argumentsJson, new TypeReference<HashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 在这里针对 arguments, 再提供一组 getter setter , 用来去更方便的获取/设置这里的键值对.
    // 这一组在 java 代码内部使用 (比如测试的时候)
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
