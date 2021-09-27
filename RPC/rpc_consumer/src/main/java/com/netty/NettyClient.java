package com.netty;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.discover.ConnectManage;
import com.entity.Request;
import com.entity.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.SocketAddress;
import java.util.concurrent.SynchronousQueue;

@Component
public class NettyClient {
    private EventLoopGroup group = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();

    @Autowired
    NettyClientHandler clientHandler;
    @Autowired
    ConnectManage connectManage;
    //构造器，初始化
    public NettyClient(){
        bootstrap.group(group).
                channel(NioSocketChannel.class).
                option(ChannelOption.TCP_NODELAY, true).
                option(ChannelOption.SO_KEEPALIVE, true).
                handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new JsonEncode());
                        pipeline.addLast(new JsonDecode());
                        pipeline.addLast(clientHandler);
                    }
                });
    }

    public Response Send(Request data) throws InterruptedException {
        Channel channel = connectManage.ChooseChannel();//在选择channel时，所有的channel已经连接好了netty服务器
        if(channel!=null && channel.isActive()){
            SynchronousQueue<Object> res_queue = clientHandler.SendRequest(data,channel);
            return (Response) res_queue.take();
        }else{
            Response response = new Response();
            response.setData("不能连接到服务器");
            return response;
        }
    }

    //连接netty服务器
    public Channel doConnect(SocketAddress address) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(address);
        Channel channel = future.sync().channel();
        return channel;
    }
}
