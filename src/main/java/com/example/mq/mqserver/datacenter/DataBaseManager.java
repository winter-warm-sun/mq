package com.example.mq.mqserver.datacenter;

import com.example.mq.MqApplication;
import com.example.mq.mqserver.core.Binding;
import com.example.mq.mqserver.core.Exchange;
import com.example.mq.mqserver.core.ExchangeType;
import com.example.mq.mqserver.core.MSGQueue;
import com.example.mq.mqserver.mapper.MetaMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/*
 * 通过这个类，来整合上述的数据库操作
 */
@Slf4j
public class DataBaseManager {
    // 要做的是从 Spring 中拿到现成的对象
    private MetaMapper metaMapper;

    // 针对数据库进行初始化
    public void init() {
        // 手动的获取到 MetaMapper
        metaMapper= MqApplication.context.getBean(MetaMapper.class);

        if(!checkDBExists()) {
            // 数据库不存在，就进行建库表操作
            // 先创建一个 data目录
            File dataDir=new File("./data");
            dataDir.mkdirs();
            // 创建数据表
            createTable();
            // 插入默认数据
            createDefaultData();
            log.info("[DataBaseManager] 数据库初始化完成！");
        }else {
            // 数据库已经存在了，不用做其他工作了
            log.info("[DataBaseManager] 数据库已经存在!");
        }
    }

    public void deleteDB() {
    }

    private boolean checkDBExists() {
        // 得到这个路径下的文件
        File file=new File("./data/meta.db");
        if(file.exists()) {
            return true;
        }
        return false;
    }

    // 该方法用来建表
    // 建库操作并不需要手动执行. （不需要手动创建 meta.db 文件）
    // 首次执行这里的数据库操作时，就会自动的创建出 meta.db 文件来（MyBatis 帮我们完成的）
    private void createTable() {
        metaMapper.createExchangeTable();
        metaMapper.createQueueTable();
        metaMapper.createBindingTable();
        log.info("[DataBaseManger] 创建表完成！");
    }

    // 给数据表中，添加默认的数据
    // 此处主要是添加一个默认的交换机
    // RabbitMQ 里有一个这样的设定：带有一个 匿名 的交换机，类型是 DIRECT
    private void createDefaultData() {
        // 构造一个默认的交换机
        Exchange exchange=new Exchange();
        exchange.setName("");
        exchange.setType(ExchangeType.DIRECT);
        exchange.setDurable(true);
        exchange.setAutoDelete(false);
        metaMapper.insertExchange(exchange);
        log.info("[DataBaseManager] 创建初始数据完成！");
    }

    // 把其他的数据库的操作，也在这个类中封装一下
    public void insertExchange(Exchange exchange) {
        metaMapper.insertExchange(exchange);
    }

    public List<Exchange> selectAllExchanges() {
        return metaMapper.selectAllExchanges();
    }

    public void deleteExchange(String exchangeName) {
        metaMapper.deleteExchange(exchangeName);
    }

    public void insertQueue(MSGQueue queue) {
        metaMapper.insertQueue(queue);
    }

    public List<MSGQueue> selectAllQueues() {
        return metaMapper.selectAllQueues();
    }

    public void deleteQueue(String queueName) {
        metaMapper.deleteQueue(queueName);
    }

    public void insertBinding(Binding binding) {
        metaMapper.insertBinding(binding);
    }

    public List<Binding> selectAllBindings() {
        return metaMapper.selectAllBindings();
    }

    public void deleteBinding(Binding binding) {
        metaMapper.deleteBinding(binding);
    }
}
