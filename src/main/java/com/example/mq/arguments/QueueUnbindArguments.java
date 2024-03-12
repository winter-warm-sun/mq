package com.example.mq.arguments;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueueUnbindArguments extends BasicArguments implements Serializable {
    private String queueName;
    private String exchangeName;
}
