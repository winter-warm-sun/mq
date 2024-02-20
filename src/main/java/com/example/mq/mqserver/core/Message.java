package com.example.mq.mqserver.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

/*
 * 表示一个要传递的消息
 * 此处的 Message 对象, 是需要能够在网络上传输, 并且也需要能写入到文件中.
 * 此时就需要针对 Message 进行序列化和反序列化.
 * 此处使用 标准库 自带的 序列化/反序列化 操作.
 */
@Data
public class Message implements Serializable {
    // 这两个属性是 Message 最核心的部分.
    private BasicProperties basicProperties = new BasicProperties();
    private byte[] body;

    // 下面的属性则是辅助用的属性.
    // Message 后续会存储到文件中(如果持久化的话).
    // 一个文件中会存储很多的消息. 如何找到某个消息, 在文件中的具体位置呢?
    // 使用下列的两个偏移量来进行表示. [offsetBeg, offsetEnd)
    // 这俩属性并不需要被序列化保存到文件中~~ 此时消息一旦被写入文件之后, 所在的位置就固定了. 并不需要单独存储.
    // 这俩属性存在的目的, 主要就是为了让内存中的 Message 对象, 能够快速找到对应的硬盘上的 Message 的位置.
    private transient long offsetBeg = 0;  // 消息数据的开头距离文件开头的位置偏移(字节)
    private transient long offsetEnd = 0;  // 消息数据的结尾距离文件开头的位置偏移(字节)
    // 使用这个属性表示该消息在文件中是否是有效消息. (针对文件中的消息, 如果删除, 使用逻辑删除的方式)
    // 0x1 表示有效. 0x0 表示无效.
    private byte isValid = 0x1;

    // 创建一个工厂方法, 让工厂方法帮我们封装一下创建 Message 对象的过程.
    // 这个方法中创建的 Message 对象, 会自动生成唯一的 MessageId
    // 万一 routingKey 和 basicProperties 里的 routingKey 冲突, 以外面的为主.
    public static Message createMessageWithId(String routingKey, BasicProperties basicProperties, byte[] body) {
        Message message = new Message();
        if (basicProperties != null) {
            message.setBasicProperties(basicProperties);
        }
        // 此处生成的 MessageId 以 M- 作为前缀.
        message.setMessageId("M-" + UUID.randomUUID());
        message.setRoutingKey(routingKey);
        message.body = body;
        // 此处是把 body 和 basicProperties 先设置出来. 他俩是 Message 的核心内容.
        // 而 offsetBeg, offsetEnd, isValid, 则是消息持久化的时候才会用到. 在把消息写入文件之前再进行设定.
        // 此处只是在内存中创建一个 Message 对象.
        return message;
    }

    public String getMessageId() {
        return basicProperties.getMessageId();
    }

    public void setMessageId(String messageId) {
        basicProperties.setMessageId(messageId);
    }

    public String getRoutingKey() {
        return basicProperties.getRoutingKey();
    }

    public void setRoutingKey(String routingKey) {
        basicProperties.setRoutingKey(routingKey);
    }

    public int getDeliverMode() {
        return basicProperties.getDeliverMode();
    }

    public void setDeliverMode(int mode) {
        basicProperties.setDeliverMode(mode);
    }
}
