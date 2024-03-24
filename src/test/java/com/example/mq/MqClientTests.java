package com.example.mq;

import com.example.mq.common.Consumer;
import com.example.mq.common.MqException;
import com.example.mq.mqclient.Channel;
import com.example.mq.mqclient.Connection;
import com.example.mq.mqclient.ConnectionFactory;
import com.example.mq.mqserver.BrokerServer;
import com.example.mq.mqserver.core.BasicProperties;
import com.example.mq.mqserver.core.ExchangeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

@Slf4j
@SpringBootTest
public class MqClientTests {
    private BrokerServer brokerServer=null;
    private ConnectionFactory factory=null;
    private Thread t=null;

    @BeforeEach
    public void setUp() throws IOException {
        // 1. 先启动服务器
        MqApplication.context= SpringApplication.run(MqApplication.class);
        brokerServer=new BrokerServer(9090);
        t=new Thread(()-> {
            // 这个 start 方法会进入一个死循环，使用一个新的线程来运行 start即可
            try {
                brokerServer.start();
            }catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();

        // 2. 配置 ConnectionFactory
        factory=new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(9090);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // 停止服务器
        brokerServer.stop();
        // t.join()
        MqApplication.context.close();

        // 删除必要的文件
        File file=new File("./data");
        FileUtils.deleteDirectory(file);
        factory=null;
    }

    @Test
    public void testConnection() throws IOException {
        Connection connection=factory.newConnection();
        Assertions.assertNotNull(connection);
    }

    @Test
    public void testChannel() throws IOException {
        Connection connection=factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel=connection.createChannel();
        Assertions.assertNotNull(channel);
    }

    @Test
    public void testExchange() throws IOException {
        Connection connection=factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel=connection.createChannel();
        Assertions.assertNotNull(channel);

        boolean ok=channel.exchangeDeclare("testExchange", ExchangeType.DIRECT,true,false,null);
        Assertions.assertTrue(ok);

        ok=channel.exchangeDelete("testExchange");
        Assertions.assertTrue(ok);

        // 此处稳妥起见 把该关闭的进行关闭
        channel.close();
        connection.close();
    }

    @Test
    public void testQueue() throws IOException {
        Connection connection=factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel=connection.createChannel();
        Assertions.assertNotNull(channel);
        boolean ok=channel.queueDeclare("testQueue",true,false,false,null);
        Assertions.assertTrue(ok);
        ok=channel.queueDelete("testQueue");
        Assertions.assertTrue(ok);

        channel.close();
        connection.close();
    }

    @Test
    public void testBinding() throws IOException {
        Connection connection=factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel=connection.createChannel();
        Assertions.assertNotNull(channel);

        boolean ok=channel.exchangeDeclare("testExchange",ExchangeType.DIRECT,true,false,null);
        Assertions.assertTrue(ok);
        ok=channel.queueDeclare("testQueue",true,false,false,null);
        Assertions.assertTrue(ok);

        ok=channel.queueBind("testQueue","testExchange","testBindingKey");
        Assertions.assertTrue(ok);

        ok=channel.queueUnbind("testQueue","testExchange");
        Assertions.assertTrue(ok);

        channel.close();
        connection.close();
    }

    @Test
    public void testMessage() throws IOException, MqException, InterruptedException {
        Connection connection=factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel=connection.createChannel();
        Assertions.assertNotNull(channel);

        boolean ok=channel.exchangeDeclare("testExchange",ExchangeType.DIRECT,true,false,null);
        Assertions.assertTrue(ok);
        ok=channel.queueDeclare("testQueue",true,false,false,null);
        Assertions.assertTrue(ok);

        byte[] requestBody="hello".getBytes();
        ok=channel.basicPublish("testExchange","testQueue",null,requestBody);
        Assertions.assertTrue(ok);

        ok=channel.basicConsume("testQueue", true, new Consumer() {
            @Override
            public void handleDelivery(String consumerTag, BasicProperties basicProperties, byte[] body) throws MqException, IOException {
                log.info("[消费数据]开始！");
                log.info("consumerTag="+consumerTag);
                log.info("basicProperties="+basicProperties);
                Assertions.assertArrayEquals(requestBody,body);
                log.info("[消费数据]结束！");
            }
        });
        Assertions.assertTrue(ok);
        Thread.sleep(500);
        channel.close();
        connection.close();
    }
}
