package com.example.mq.demo;

import com.example.mq.common.Consumer;
import com.example.mq.common.MqException;
import com.example.mq.mqclient.Channel;
import com.example.mq.mqclient.Connection;
import com.example.mq.mqclient.ConnectionFactory;
import com.example.mq.mqserver.core.BasicProperties;
import com.example.mq.mqserver.core.ExchangeType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/*
 *  这个类表示一个消费者
 *  通常这个类也应该是在一个独立的服务器中被执行
 */
@Slf4j
public class DemoConsumer {
    public static void main(String[] args) throws IOException, MqException, InterruptedException {
        log.info("启动消费者！");
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(9090);

        Connection connection=factory.newConnection();
        Channel channel=connection.createChannel();

        channel.exchangeDeclare("testExchange", ExchangeType.DIRECT,true,false,null);
        channel.queueDeclare("testQueue",true,false,false,null);
        channel.basicConsume("testQueue", true, new Consumer() {
            @Override
            public void handleDelivery(String consumerTag, BasicProperties basicProperties, byte[] body) throws MqException, IOException {
                log.info("[消费数据] 开始！");
                log.info("consumerTag="+consumerTag);
                log.info("basicProperties="+basicProperties);
                String bodyString=new String(body,0,body.length);
                log.info("body="+bodyString);
                log.info("[消费数据]结束！");
            }
        });

        // 由于消费者也不知道生产者要生产多少，就在这里通过这个循环模拟一直等待消费
        while (true) {
            Thread.sleep(500);
        }
    }
}
