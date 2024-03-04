package com.example.mq.mqserver;

import com.example.mq.common.MqException;
import com.example.mq.mqserver.core.*;
import com.example.mq.mqserver.datacenter.DiskDataCenter;
import com.example.mq.mqserver.datacenter.MemoryDataCenter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 通过这个类，来表示虚拟主机
 * 每个虚拟主机下面都管理着自己的交换机、队列、绑定、消息数据
 * 同时提供 api 供上层调用
 * 针对 VirtualHost 这个类，作为业务逻辑的整合者，就需要对于代码中抛出的异常进行处理了
 */
@Slf4j
public class VirtualHost {
    private String virtualHostName;
    private MemoryDataCenter memoryDataCenter=new MemoryDataCenter();
    private DiskDataCenter diskDataCenter=new DiskDataCenter();
    private Router router=new Router();

    // 操作交换机的锁对象
    private final Object exchangeLocker=new Object();

    // 操作队列的锁对象
    private final Object queueLocker=new Object();

    public String getVirtualHostName() {
        return virtualHostName;
    }

    public MemoryDataCenter getMemoryDataCenter() {
        return memoryDataCenter;
    }

    public DiskDataCenter getDiskDataCenter() {
        return diskDataCenter;
    }

    public VirtualHost(String name) {
        this.virtualHostName=name;

        // 对于 MemoryDataCenter 来说，不需要额外的初始化操作，只要对象 new出来就行了
        // 但是，针对 DiskDataCenter 来说，则需要进行初始化操作，建库建表和初始数据的设定
        diskDataCenter.init();

        // 另外还需要针对硬盘的数据，进行恢复到内存中
        try {
            memoryDataCenter.recovery(diskDataCenter);
        } catch (IOException | MqException | ClassNotFoundException e) {
            e.printStackTrace();
            log.info("[VirtualHost] 恢复内存数据失败！");
        }
    }

    // 创建交换机
    // 如果交换机不存在，就创建；如果存在，直接返回
    // 返回值是 boolean,创建成功，返回 true；失败返回 false
    public boolean exchangeDeclare(String exchangeName, ExchangeType exchangeType, boolean durable, boolean autoDelete,
                                   Map<String,Object> arguments) {
        // 把交换机的名字，加上虚拟主机名作为前缀
        exchangeName=virtualHostName+exchangeName;
        try {
            synchronized (exchangeLocker) {
                // 1. 判定交换机是否已经存在，直接通过内存查询
                Exchange existsExchange=memoryDataCenter.getExchange(exchangeName);
                if(existsExchange!=null) {
                    // 该交换机已经存在
                    log.info("[VirtualHost] 交换机已经存在！ exchangeName="+exchangeName);
                    return true;
                }
                // 2. 真正创建交换机，先构造 Exchange 对象
                Exchange exchange=new Exchange();
                exchange.setName(exchangeName);
                exchange.setType(exchangeType);
                exchange.setDurable(durable);
                exchange.setAutoDelete(autoDelete);
                exchange.setArguments(arguments);
                // 3. 把交换机对象写入硬盘
                if(durable) {
                    diskDataCenter.insertExchange(exchange);
                }
                // 4. 把交换机对象写入内存
                memoryDataCenter.insertExchange(exchange);
                log.info("[VirtualHost] 交换机创建完成！ exchangeName="+exchangeName);
                // 上述逻辑，先写硬盘，后写内存，目的是因为硬盘更容易写失败，如果硬盘写失败了，内存就不写了
                // 要是先写内存，内存写成功了，硬盘写失败了，还需要把内存的数据给再删掉，就比较麻烦了。
            }
            return true;
        }catch (Exception e) {
            log.info("[VirtualHost] 交换机创建失败！ exchangeName="+exchangeName);
            e.printStackTrace();
            return false;
        }
    }

    // 删除交换机
    public boolean exchangeDelete(String exchangeName) {
        exchangeName=virtualHostName+exchangeName;
        try {
            synchronized (exchangeLocker) {
                // 1. 先找到对应的交换机
                Exchange toDelete=memoryDataCenter.getExchange(exchangeName);
                if(toDelete==null) {
                    throw new MqException("[VirtualHost] 交换机不存在无法删除！");
                }
                // 2. 删除硬盘上的数据
                if(toDelete.isDurable()) {
                    diskDataCenter.deleteExchange(exchangeName);
                }
                // 3. 删除内存中的交换机数据
                memoryDataCenter.deleteExchange(exchangeName);
                log.info("[VirtualHost] 交换机删除成功！ exchangeName="+exchangeName);
            }
            return true;
        }catch (Exception e) {
            log.info("[VirtualHost] 交换机删除失败！exchangeName=" +exchangeName);
            e.printStackTrace();
            return false;
        }
    }

    // 创建队列
    public boolean queueDeclare(String queueName,boolean durable,boolean exclusive,boolean autoDelete,
                                Map<String,Object> arguments) {
        // 把队列的名字，给拼接上虚拟主机的名字
        queueName=virtualHostName+queueName;
        try {
            synchronized (queueLocker) {
                // 1. 判定队列是否存在
                MSGQueue existQueue=memoryDataCenter.getQueue(queueName);
                if(existQueue!=null) {
                    log.info("[VirtualHost] 队列已经存在！ exchangeName="+queueName);
                    return true;
                }
                // 2. 创建队列对象
                MSGQueue queue=new MSGQueue();
                queue.setName(queueName);
                queue.setDurable(durable);
                queue.setExclusive(exclusive);
                queue.setAutoDelete(autoDelete);
                queue.setArguments(arguments);
                // 3. 写硬盘
                if (durable) {
                    diskDataCenter.insertQueue(queue);
                }
                // 4. 写内存
                memoryDataCenter.insertQueue(queue);
                log.info("[VirtualHost] 队列创建成功！ queueName="+queueName);
            }
            return true;
        } catch (Exception e) {
            log.info("[VirtualHost] 队列创建失败！ queueName="+queueName);
            e.printStackTrace();
            return false;
        }
    }

    // 删除队列
    public boolean queueDelete(String queueName) {
        queueName=virtualHostName+queueName;
        try {
            synchronized (queueLocker) {
                // 1. 根据队列名字，查询下当前的队列对象
                MSGQueue queue=memoryDataCenter.getQueue(queueName);
                if(queue==null) {
                    throw new MqException("[VirtualHost] 队列不存在！无法删除！queueName="+queueName);
                }
                // 2. 删除硬盘数据
                if(queue.isDurable()) {
                    diskDataCenter.deleteQueue(queueName);
                }
                // 3. 删除内存数据
                memoryDataCenter.deleteQueue(queueName);
                log.info("[VirtualHost] 删除队列成功！queueName="+queueName);
            }
            return true;
        } catch (Exception e) {
            log.info("[VirtualHost] 删除队列失败！ queueName="+queueName);
            e.printStackTrace();
            return false;
        }
    }

    // 增加绑定
    public boolean queueBind(String queueName,String exchangeName,String bindingKey) {
        queueName=virtualHostName+queueName;
        exchangeName=virtualHostName+queueName;
        try {
            synchronized (exchangeLocker) {
                synchronized (queueLocker) {
                    // 1. 判定当前的绑定是否已经存在了
                    Binding existsBinding=memoryDataCenter.getBinding(exchangeName,queueName);
                    if(existsBinding!=null) {
                        throw new MqException("[VirtualHost] binding 已经存在！ queueName="+queueName
                                +", exchangeName="+exchangeName);
                    }
                    // 2. 验证 bindingKey 是否合法
                    if (!router.checkBindingKey(bindingKey)) {
                        throw new MqException("[VirtualHost] bindingKey 非法！ bindingKey="+bindingKey);
                    }
                    // 3. 创建 Binding 对象
                    Binding binding=new Binding();
                    binding.setExchangeName(exchangeName);
                    binding.setQueueName(queueName);
                    binding.setBindingKey(bindingKey);
                    // 4. 获取一下对应的交换机和队列，如果交换机或者队列不存在，这样的绑定也是无法创建的
                    MSGQueue queue=memoryDataCenter.getQueue(queueName);
                    if(queue==null) {
                        throw new MqException("[VirtualHost] 队列不存在！ queueName=" + queueName);
                    }
                    Exchange exchange=memoryDataCenter.getExchange(exchangeName);
                    if(exchange==null) {
                        throw new MqException("[VirtualHost] 交换机不存在！ exchangeName=" + exchangeName);
                    }
                    // 5. 先写硬盘
                    if(queue.isDurable()&&exchange.isDurable()) {
                        diskDataCenter.insertBinding(binding);
                    }
                    // 6. 再写入内存
                    memoryDataCenter.insertBinding(binding);
                }
            }
            log.info("[VirtualHost] 绑定创建成功！ exchangeName="+exchangeName
            +", queueName="+queueName);
            return true;
        } catch (Exception e) {
            log.info("[VirtualHost] 绑定创建失败！ exchangeName="+exchangeName
            +", queueName="+queueName);
            e.printStackTrace();
            return false;
        }
    }

    // 解除绑定
    public boolean queueUnbind(String queueName,String exchangeName) {
        queueName=virtualHostName+queueName;
        exchangeName=virtualHostName+exchangeName;
        try {
            synchronized (exchangeLocker) {
                synchronized (queueLocker) {
                    // 1. 获取binding 看是否已经存在
                    Binding binding=memoryDataCenter.getBinding(exchangeName,queueName);
                    if(binding==null) {
                        throw new MqException("[VirtualHost] 删除绑定失败！ 绑定不存在！ exchangeName="+exchangeName+", queueName"+queueName);
                    }
                    // 2. 无论绑定是否持久化了，都尝试从硬盘删一下，就算不存在，这个删除也无副作用
                    diskDataCenter.deleteBinding(binding);
                    // 3. 删除内存的数据
                    memoryDataCenter.deleteBinding(binding);
                    log.info("[VirtualHost] 删除绑定成功！");
                }
            }
            return true;
        } catch (Exception e) {
            log.info("[VirtualHost] 删除绑定失败！");
            e.printStackTrace();
            return false;
        }
    }

    // 发送消息到指定的交换机/队列中
    public boolean basicPublish(String exchangeName,String routingKey,BasicProperties basicProperties,byte[] body) {
        try {
            // 1. 转换交换机的名字
            exchangeName=virtualHostName+exchangeName;
            // 2. 检查 routingKey 是否合法
            if (router.checkRoutingKey(routingKey)) {
                throw new MqException("[VirtualHost] routingKey 非法！routingKey="+routingKey);
            }
            // 3. 查找交换机对象
            Exchange exchange=memoryDataCenter.getExchange(exchangeName);
            if (exchange==null) {
                throw new MqException("[VirtualHost] 交换机不存在！ exchangeName="+exchangeName);
            }
            // 4. 判断交换机的类型
            if(exchange.getType()==ExchangeType.DIRECT) {
                // 按照直接交换机的方式来转发消息
                // 以 routingKey 作为队列的名字，直接把消息写入到指定的队列中
                // 此时 可以无视绑定关系
                String queueName=virtualHostName+routingKey;
                // 5. 构造消息对象
                Message message=Message.createMessageWithId(routingKey, basicProperties, body);
                // 6. 查找该队列名对应的对象
                MSGQueue queue=memoryDataCenter.getQueue(queueName);
                if(queue==null) {
                    throw new MqException("[VirtualHost] 队列不存在！queueName="+queueName);
                }
                // 7. 队列存在，直接给队列中写入消息
                sendMessage(queue,message);
            } else {
                // 按照 fanout 和 topic 的方式来转发
                // 找到该交换机关联的所有绑定，并遍历这些绑定对象
                ConcurrentHashMap<String,Binding> bindingsMap=memoryDataCenter.getBindings(exchangeName);
                for (Map.Entry<String,Binding> entry: bindingsMap.entrySet()) {
                    // 1) 获取到绑定对象，判定对应的队列是否存在
                    Binding binding=entry.getValue();
                    MSGQueue queue=memoryDataCenter.getQueue(binding.getQueueName());
                    if(queue==null) {
                        // 此处就不抛出异常了 可能此处有多个这样的队列
                        // 希望不要因为一个队列的失败，影响到其他队列的消息的传输
                        log.info("[VirtualHost] basicPublish 发送消息时，发现队列不存在！");
                        continue;
                    }
                    // 2) 构造消息对象
                    Message message=Message.createMessageWithId(routingKey,basicProperties,body);
                    // 3) 判定这个消息是否能转发给该队列
                    //   如果是 fanout, 所有绑定的队列都要转发
                    //   如果是 topic，还需要判定下，bindingKey 和 routingKey是不是匹配
                    if(!router.route(exchange.getType(),binding,message)) {
                        continue;
                    }
                    // 4) 真正转发消息给队列
                    sendMessage(queue,message);
                }
            }
            return true;
        } catch (Exception e) {
            log.info("[VirtualHost] 消息发送失败！");
            e.printStackTrace();
            return false;
        }
    }

    private void sendMessage(MSGQueue queue,Message message) throws IOException, MqException {
        // 此处发送消息，就是把消息写入到 硬盘和内存上
        int deliverMode=message.getDeliverMode();
        // deliverMode 为1，不持久化；deliverMode 为2，表示持久化
        if (deliverMode==2) {
            diskDataCenter.sendMessage(queue,message);
        }
        // 写入内存
        memoryDataCenter.sendMessage(queue,message);

        // 此处还需要补充一个逻辑，通知消费者可以消息消息了

    }

}
