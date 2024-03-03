package com.example.mq.mqserver;

import com.example.mq.common.MqException;
import com.example.mq.mqserver.core.Exchange;
import com.example.mq.mqserver.core.ExchangeType;
import com.example.mq.mqserver.datacenter.DiskDataCenter;
import com.example.mq.mqserver.datacenter.MemoryDataCenter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

import java.io.IOException;
import java.util.Map;

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
            return true;
        }catch (Exception e) {
            log.info("[VirtualHost] 交换机创建失败！ exchangeName="+exchangeName);
            e.printStackTrace();
            return false;
        }
    }

    // 删除交换机
    public boolean exchangeDelete() {

    }
}
