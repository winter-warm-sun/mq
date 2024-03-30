package com.example.mq.mqclient;

import com.example.mq.common.BinaryTool;
import com.example.mq.common.MqException;
import com.example.mq.common.Request;
import com.example.mq.common.Response;
import com.example.mq.returns.BasicReturns;
import com.example.mq.returns.SubScribeReturns;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Connection {
    private Socket socket=null;
    // 需要管理多个 channel，使用一个 hash 表把若干个 channel 组织起来
    private ConcurrentHashMap<String,Channel> channelMap=new ConcurrentHashMap<>();

    private InputStream inputStream;
    private OutputStream outputStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private ExecutorService callbackPool=null;

    public Connection(String host,int port) throws IOException {
        socket=new Socket(host,port);
        inputStream= socket.getInputStream();
        outputStream=socket.getOutputStream();
        dataInputStream=new DataInputStream(inputStream);
        dataOutputStream=new DataOutputStream(outputStream);
        callbackPool= Executors.newFixedThreadPool(4);

        // 创建一个扫描线程，由这个线程负责不停的从 socket 中读取响应数据，把这个响应数据再交给对应的 channel 负责处理
        Thread t=new Thread(()-> {
            try {
                while (!socket.isClosed()) {
                    Response response=readResponse();
                    dispatchResponse(response);
                }
            }  catch (SocketException e) {
                // 连接正常断开，此时这个异常直接忽略
                log.info("[Connection] 连接正常断开");
            }  catch (IOException|ClassNotFoundException|MqException e) {
                log.info("[Connection] 连接异常断开");
                e.printStackTrace();
            }
        });
        t.start();
    }

    public void close() {
        // 关闭 Connection 释放上述资源
        try {
            callbackPool.shutdown();
            channelMap.clear();
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 使用这个方法来分别处理，当前的响应是一个针对控制请求的响应，还是服务器推送的消息
    private void dispatchResponse(Response response) throws IOException, ClassNotFoundException, MqException {
        if(response.getType()==0xc) {
            // 服务器推送来的消息数据
            SubScribeReturns subScribeReturns=(SubScribeReturns) BinaryTool.fromBytes(response.getPayload());
            // 根据 channelId 找到对应的 channel 对象
            Channel channel=channelMap.get(subScribeReturns.getChannelId());
            if(channel==null) {
                throw new MqException("[Connection] 该消息对应的 channel在客户度中不存在！channelId="+channel.getChannelId());
            }
            // 执行该 channel 对象内部的回调
            callbackPool.submit(()-> {
                try {
                    channel.getConsumer().handleDelivery(subScribeReturns.getConsumerTag(),subScribeReturns.getBasicProperties(),
                            subScribeReturns.getBody());
                } catch (MqException | IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
           // 当前响应是针对刚才的控制请求的响应
            BasicReturns basicReturns=(BasicReturns) BinaryTool.fromBytes(response.getPayload());
            // 把这个结果放到对应的 channel 的hash表中
            Channel channel=channelMap.get(basicReturns.getChannelId());
            if(channel==null) {
                throw new MqException("[Connection] 该消息对应的 channel在客户端中不存在！channelId="+channel.getChannelId());
            }
            channel.putReturns(basicReturns);
        }
    }

    // 发送请求
    public void writeRequest(Request request) throws IOException {
        dataOutputStream.writeInt(request.getType());
        dataOutputStream.writeInt(request.getLength());
        dataOutputStream.write(request.getPayload());
        dataOutputStream.flush();
        log.info("[Connection] 发送请求！type="+request.getType()+", length="+request.getLength());
    }
    // 读取响应
    public Response readResponse() throws IOException {
        Response response=new Response();
        response.setType(dataInputStream.readInt());
        response.setLength(dataInputStream.readInt());
        byte[] payload=new byte[response.getLength()];
        int n=dataInputStream.read(payload);
        if(n!=response.getLength()) {
            throw new IOException("读取的响应数据不完整！");
        }
        response.setPayload(payload);
        log.info("[Connection] 收到响应！type="+response.getType()+",length="+response.getLength());
        return response;
    }

    // 通过这个方法，在 Connection 中能够创建出一个 Channel
    public Channel createChannel() throws IOException {
        String channelId="C-"+ UUID.randomUUID().toString();
        Channel channel=new Channel(channelId,this);
        // 把这个 channel 对象放到 Connection 管理 channel 的哈希表中
        channelMap.put(channelId,channel);
        // 同时也需要把“创建 channel”的这个消息告诉服务器
        boolean ok=channel.createChannel();
        if (!ok) {
           // 服务器这里创建失败了！！整个这次创建 channel 操作不顺利
           // 把刚才已经加入 hash 表的键值对，再删了
           channelMap.remove(channelId);
           return null;
        }
        return channel;
    }
}
