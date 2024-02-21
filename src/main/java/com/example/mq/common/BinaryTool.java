package com.example.mq.common;

import java.io.*;

//下面的逻辑，并不仅仅是Message，其他的Java中的对象，也可以通过这样的逻辑进行序列化和反序列化
// 如果要想让这个对象能够序列化或者反序列化，需要让类能够实现 Serializable 接口
public class BinaryTool {
    // 把一个对象序列化成一个字节数据
    public static byte[] toBytes(Object object) throws IOException {
        // 这个流对象相当于一个变长的字节数据
        // 就可以把 object 序列化的数据给逐渐写入到 byteArrayOutputStream 中，再统一转成 byte[]
        try (ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream()){
            try (ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream)){
                // 此处的 writeObject 会把该对象进行序列化，生成的二进制字节数据，
                // 就会写入到 ObjectOutputStream 中
                // 由于 ObjectOutputStream 又关联到了 ByteArrayOutputStream,
                // 最终结果就写入到 ByteArrayOutputStream 里了
                objectOutputStream.writeObject(object);
            }
            // 该操作把 byteArrayOutputStream 中持有的二进制数据取出来，转换成 byte[]
            return byteArrayOutputStream.toByteArray();
        }
    }

    // 把一个字节数组，反序列化成一个对象
    public static Object fromBytes(byte[] data) throws IOException, ClassNotFoundException {
        Object object=null;
        try(ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(data)){
            try(ObjectInputStream objectInputStream=new ObjectInputStream(byteArrayInputStream)){
                // 此处的 readObject,就是从data这个 byte[] 中读取数据并进行反序列化
                object=objectInputStream.readObject();
            }
        }
        return object;
    }
}
