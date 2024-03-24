package com.example.mq.mqclient;

import com.example.mq.arguments.*;
import com.example.mq.common.BinaryTool;
import com.example.mq.common.Consumer;
import com.example.mq.common.MqException;
import com.example.mq.common.Request;
import com.example.mq.mqserver.core.BasicProperties;
import com.example.mq.mqserver.core.ExchangeType;
import com.example.mq.returns.BasicReturns;
import lombok.Data;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Channel {
    private String channelId;
    // 当前这个 channel 属于哪个连接
    private Connection connection;
    // 用来存储后续客户端收到的服务器的响应
    private ConcurrentHashMap<String, BasicReturns> basicReturnsMap=new ConcurrentHashMap<>();  // key:Rid
    // 如果当前 Channel 订阅了某个队列，就需要在此处记录下对应回调是啥，当该队列的消息返回回来的时候，调用回调
    // 此处约定一个 Channel 只能有一个回调
    private Consumer consumer=null;

    public Channel(String channelId,Connection connection) {
        this.channelId=channelId;
        this.connection=connection;
    }

    // 在这个方法中，和服务器进行交互，告知服务器，此处客户端创建了新的 channel了
    public boolean createChannel() throws IOException {
        // 对于创建 Channel 操作来说，payload 就是一个 basicArguments 对象
        BasicAckArguments basicAckArguments=new BasicAckArguments();
        basicAckArguments.setChannelId(channelId);
        basicAckArguments.setRid(generateRid());
        byte[] payload= BinaryTool.toBytes(basicAckArguments);

        Request request=new Request();
        request.setType(0x1);
        request.setLength(payload.length);
        request.setPayload(payload);

        // 构造出完整请求之后，就可以发送这个请求了
        connection.writeRequest(request);
        // 等待服务器的响应
        BasicReturns basicReturns=waitResult(basicAckArguments.getRid());
        return basicReturns.isOk();
    }

    // 期望使用这个方法来阻塞等待服务器的响应
    private BasicReturns waitResult(String rid) {
        BasicReturns basicReturns=null;
        while ((basicReturns=basicReturnsMap.get(rid))==null) {
            // 如果查询结果为null,说明包裹还没回来
            // 此时需要阻塞等待
            synchronized (this) {
                try {
                    wait();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // 读取成功之后，还需要把这个消息从哈希表中删除掉
        basicReturnsMap.remove(rid);
        return basicReturns;
    }

    public void putReturns(BasicReturns basicReturns) {
        basicReturnsMap.put(basicReturns.getRid(),basicReturns);
        synchronized (this) {
            // 当前也不知道有多少个线程在等待上述的这个响应
            // 把所有的等待的线程都唤醒
            notifyAll();
        }
    }

    private String generateRid() {
        return "R-"+ UUID.randomUUID().toString();
    }

    // 关闭channel，给服务器发送一个 type=0x2 的请求
    public boolean close() throws IOException {
        BasicArguments basicArguments=new BasicArguments();
        basicArguments.setRid(generateRid());
        basicArguments.setChannelId(channelId);
        byte[] payload=BinaryTool.toBytes(basicArguments);

        Request request=new Request();
        request.setType(0x2);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(basicArguments.getRid());
        return basicReturns.isOk();
    }

    // 创建交换机    type=0x3
    public boolean exchangeDeclare(String exchangeName, ExchangeType exchangeType, boolean durable, boolean autoDelete,
                                   Map<String,Object> arguments) throws IOException {
        ExchangeDeclareArguments exchangeDeclareArguments=new ExchangeDeclareArguments();
        exchangeDeclareArguments.setRid(generateRid());
        exchangeDeclareArguments.setChannelId(channelId);
        exchangeDeclareArguments.setExchangeName(exchangeName);
        exchangeDeclareArguments.setExchangeType(exchangeType);
        exchangeDeclareArguments.setDurable(durable);
        exchangeDeclareArguments.setAutoDelete(autoDelete);
        exchangeDeclareArguments.setArguments(arguments);
        byte[] payload=BinaryTool.toBytes(exchangeDeclareArguments);

        Request request=new Request();
        request.setType(0x3);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(exchangeDeclareArguments.getRid());
        return basicReturns.isOk();
    }

    // 删除交换机
    public boolean exchangeDelete(String exchangeName) throws IOException {
        ExchangeDeclareArguments arguments=new ExchangeDeclareArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setExchangeName(exchangeName);
        byte[] payload=BinaryTool.toBytes(arguments);
        Request request=new Request();
        request.setType(0x4);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 创建队列
    public boolean queueDeclare(String queueName,boolean durable,boolean exclusive,boolean autoDelete,
                                Map<String,Object> arguments) throws IOException {
        QueueDeclareArguments queueDeclareArguments=new QueueDeclareArguments();
        queueDeclareArguments.setRid(generateRid());
        queueDeclareArguments.setChannelId(channelId);
        queueDeclareArguments.setQueueName(queueName);
        queueDeclareArguments.setDurable(durable);
        queueDeclareArguments.setExclusive(exclusive);
        queueDeclareArguments.setAutoDelete(autoDelete);
        queueDeclareArguments.setArguments(arguments);
        byte[] payload=BinaryTool.toBytes(queueDeclareArguments);

        Request request=new Request();
        request.setType(0x5);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(queueDeclareArguments.getRid());
        return basicReturns.isOk();
    }

    // 删除队列
    public boolean queueDelete(String queueName) throws IOException {
        QueueDeleteArguments arguments=new QueueDeleteArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        byte[] payload=BinaryTool.toBytes(arguments);

        Request request=new Request();
        request.setType(0x6);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 创建绑定
    public boolean queueBind(String queueName,String exchangeName,String bindingKey) throws IOException {
        QueueBindArguments arguments=new QueueBindArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setExchangeName(exchangeName);
        arguments.setBindingKey(bindingKey);
        byte[] payload=BinaryTool.toBytes(arguments);
        Request request=new Request();
        request.setType(0x7);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 解除绑定
    public boolean queueUnbind(String queueName,String exchangeName) throws IOException {
        QueueUnbindArguments arguments=new QueueUnbindArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setExchangeName(exchangeName);
        byte[] payload=BinaryTool.toBytes(arguments);

        Request request=new Request();
        request.setType(0x8);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 发送消息
    public boolean basicPublish(String exchangeName, String routingKey, BasicProperties basicProperties,byte[] body) throws IOException {
        BasicPublishArguments arguments=new BasicPublishArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setExchangeName(exchangeName);
        arguments.setRoutingKey(routingKey);
        arguments.setBasicProperties(basicProperties);
        arguments.setBody(body);
        byte[] payload=BinaryTool.toBytes(arguments);

        Request request=new Request();
        request.setType(0x9);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 订阅消息
    public boolean basicConsume(String queueName,boolean autoAck,Consumer consumer) throws MqException, IOException {
        // 先设置回调
        if(this.consumer!=null) {
            throw new MqException("该 channel已经设置过消费消息的回调了，不能重复设置！");
        }
        this.consumer=consumer;

        BasicConsumeArguments arguments=new BasicConsumeArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setConsumerTag(channelId);
        arguments.setQueueName(queueName);
        arguments.setAutoAck(autoAck);
        byte[] payload=BinaryTool.toBytes(arguments);

        Request request=new Request();
        request.setType(0xa);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 确认消息
    public boolean basicAck(String queueName,String messageId) throws IOException {
        BasicAckArguments arguments=new BasicAckArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setMessageId(messageId);
        byte[] payload=BinaryTool.toBytes(arguments);

        Request request=new Request();
        request.setType(0xb);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns=waitResult(arguments.getRid());
        return basicReturns.isOk();
    }
}
