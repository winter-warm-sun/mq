package com.example.mq.mqclient;

import com.example.mq.common.Request;
import com.example.mq.common.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class Connection {
    private Socket socket=null;
    // 需要管理多个 channel，使用一个 hash 表把若干个 channel 组织起来
    private ConcurrentHashMap<String,Channel> channelMap=new ConcurrentHashMap<>();

    private InputStream inputStream;
    private OutputStream outputStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public Connection(String host,int port) throws IOException {
        socket=new Socket(host,port);
        inputStream= socket.getInputStream();
        outputStream=socket.getOutputStream();
        dataInputStream=new DataInputStream(inputStream);
        dataOutputStream=new DataOutputStream(outputStream);
    }

    // 发送请求
    public void writeRequest(Request request) throws IOException {
        dataOutputStream.writeInt(request.getType());
        dataOutputStream.writeInt(request.getLength());
        dataOutputStream.write(request.getPayload());
        dataOutputStream.flush();
        log.info("[Connection] 发送请求！ type="+request.getType()+", length="+request.getLength());
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
}
