package com.netty;

import com.alibaba.fastjson.JSONObject;
import com.discover.ConnectManage;
import com.entity.Request;
import com.entity.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

@Component
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    ConnectManage connectManage;

    //SynchronousQueue： 当它生产产品（即put的时候），如果当前没有人想要消费产品(即当前没有线程执行take)，此生产线程必须阻塞
    // ，等待一个消费线程调用take操作，take操作将会唤醒该生产线程，同时消费线程会获取生产线程的产品（即数据传递），
    // 这样的一个过程称为一次配对过程(当然也可以先take后put,原理是一样的)。

    //ConcurrentHashMap将数据分为多段，并且每段都加锁，实现高并发操作
    private ConcurrentHashMap<String, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    private Response response;

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("已连接到RPC服务器:"+channelHandlerContext.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        InetSocketAddress address =(InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        System.out.println("与RPC服务器断开连接:"+address);
        channelHandlerContext.channel().close();
        connectManage.RemoveChannel(channelHandlerContext.channel());//删除map中的channel
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        response = JSONObject.parseObject(msg.toString(), Response.class);//返回值数据
        SynchronousQueue<Object> queue = queueMap.get(String.valueOf(response.getId()));//根据key值来获取与put相对应的value值，实现消息queue对接
        queue.put(response);
        queueMap.remove(String.valueOf(response.getId()));
        System.out.println("响应数据为:"+response);
    }

    public SynchronousQueue<Object> SendRequest(Request request, Channel channel){
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        queueMap.put(String.valueOf(request.getId()),queue);
        channel.writeAndFlush(request);//发送请求数据。
        return queue;
    }
}
