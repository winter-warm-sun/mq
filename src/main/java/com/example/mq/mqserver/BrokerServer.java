package com.example.mq.mqserver;

import lombok.Data;
import org.apache.catalina.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/*
 * 这个 BrokerServer 就是咱们消息队列本体服务器
 * 本质上就是一个 TCP的服务器
 */
@Data
public class BrokerServer {
    private ServerSocket serverSocket=null;

    // 当前考虑一个 BrokerServer 上只有一个 虚拟主机
    private VirtualHost virtualHost=new VirtualHost("default");
    // 使用这个 哈希表 表示当前的所有会话（也就是说有哪些客户端正在和咱们的服务器进行通信）
    // 此处的 Key 是 channelId,value 为对应的 Socket对象
    private ConcurrentHashMap<String, Socket> sessions=new ConcurrentHashMap<>();
    // 引入一个线程池，来处理多个客户端的请求
    private ExecutorService executorService=null;
    // 引入一个 boolean 变量控制服务器是否继续运行
    private volatile boolean runnable=true;

    public BrokerServer(int port) throws IOException {
        serverSocket=new ServerSocket(port);
    }

    public void start() {

    }

}
