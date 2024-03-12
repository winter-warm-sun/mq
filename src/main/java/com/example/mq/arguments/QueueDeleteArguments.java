package com.example.mq.arguments;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueueDeleteArguments extends BasicArguments implements Serializable {
    private String queueName;
}
