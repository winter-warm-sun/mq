package com.example.mq.arguments;

import com.example.mq.mqserver.core.BasicProperties;
import lombok.Data;

import java.io.Serializable;

@Data
public class BasicPublishArguments extends BasicArguments implements Serializable {
    private String exchangeName;
    private String routingKey;
    private BasicProperties basicProperties;
    private byte[] body;
}
