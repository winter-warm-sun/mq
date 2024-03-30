package com.example.mq.returns;

import com.example.mq.mqserver.core.BasicProperties;
import lombok.Data;

import java.io.Serializable;

@Data
public class SubScribeReturns extends BasicReturns implements Serializable {
    private String consumerTag;
    private BasicProperties basicProperties;
    private byte[] body;
}
