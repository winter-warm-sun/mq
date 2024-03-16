package com.example.mq.mqclient;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

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
}
