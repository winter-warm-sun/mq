package com.example.mq.demo;

import com.example.mq.mqclient.Channel;
import com.example.mq.mqclient.Connection;
import com.example.mq.mqclient.ConnectionFactory;
import com.example.mq.mqserver.core.ExchangeType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/*
 * 这个类用来表示一个生产者
 * 通常这是一个单独的服务器程序
 */
@Slf4j
public class DemoProducer {
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("启动生产者");
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(9090);

        Connection connection=factory.newConnection();
        Channel channel=connection.createChannel();

        // 创建交换机和队列
        channel.exchangeDeclare("testExchange", ExchangeType.DIRECT,true,false,null);
        channel.queueDeclare("testQueue",true,false,false,null);

        // 创建一个消息未发送
        byte[] body="hello".getBytes();
        boolean ok=channel.basicPublish("testExchange","testQueue",null,body);
        log.info("消息投递完成！ ok="+ok);

        Thread.sleep(500);
        channel.close();
        connection.close();
    }
}
