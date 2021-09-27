//该类主要用于根据zookeeper服务器节点上的数据（netty服务器地址） 来连接netty服务器，
//并且把能连接上的channel放起来，并且当节点断开或者数据更新时，再来更新这些可用的channel，
//把这些channel储存起来后，便于netty客户端选用其中一条channel来完成数据发送
package com.discover;

import com.netty.NettyClient;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ConnectManage {

    private AtomicInteger roundRobin = new AtomicInteger(0);
    private CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<>();//放着所有可以使用的channel（已经连接好了）
    private Map<SocketAddress, Channel> channelNodes = new ConcurrentHashMap<>();//存放着channel和其服务器的地址

    @Autowired
    NettyClient nettyClient;

    public Channel ChooseChannel(){
        if (channels.size()>0) {
            int size = channels.size();
            int index = (roundRobin.getAndAdd(1) + size) % size;
            return channels.get(index);
        }else{
            return null;
        }
    }

    //在服务器启动时（和有节点数据变更时）更新有哪些可用的netty连接，并且加入到channels中
    public synchronized void updateConnectServer(List<String> addressList) throws InterruptedException {
        //如果zookeeper上没有节点数据
        if (addressList.size()==0 || addressList==null){
            for (final Channel channel : channels) {
                SocketAddress remotePeer = channel.remoteAddress();
                Channel handler_node = channelNodes.get(remotePeer);
                handler_node.close();
            }
            channels.clear();
            channelNodes.clear();
            return;
        }

        HashSet<SocketAddress> newAllServerNodeSet = new HashSet<>();
        for (String s : addressList) {
            String host = s.split(":")[0];
            int port = Integer.parseInt(s.split(":")[1]);
            final SocketAddress remoteAddress = new InetSocketAddress(host, port);

            if (channelNodes.get(remoteAddress) == null) {
                Channel channel = nettyClient.doConnect(remoteAddress);
                channels.add(channel);
                channelNodes.put(remoteAddress, channel);
            }
        }
    }

    //移除断开连接的channel
    public void RemoveChannel(Channel channel){
        SocketAddress remotePeer = channel.remoteAddress();
        channelNodes.remove(remotePeer);
        channels.remove(channel);
    }
}
