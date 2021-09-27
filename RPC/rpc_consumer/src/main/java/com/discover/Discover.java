//该类主要是连接zookeeper服务器，并且获得节点上的数据，并且不断监听这些数据
package com.discover;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class Discover {

    @Value("${zookeeper.address}")
    private String ZooKeeperAddress;
    private ZkClient client;
    private String PATH = "/rpc";
    private volatile List<String> addressList = new ArrayList<>();//所有节点的数据（也就是npc的服务地址）都放在这

    @Autowired
    ConnectManage connectManage;

    @PostConstruct  //该注解使该方法在服务器启动时被运行一次
    public void init() throws InterruptedException {
        client = new ZkClient(ZooKeeperAddress,2000,2000);

        //监听节点是否有变化
        List<String> nodeList = client.subscribeChildChanges(PATH, (s, nodes) -> {
            //若有变化
            addressList.clear();
            for(String node:nodes){
                String Netty_address = client.readData(PATH+"/"+node);
                addressList.add(Netty_address);
            }
            connectManage.updateConnectServer(addressList);
        });
        getNodeData(nodeList);
        connectManage.updateConnectServer(addressList);
    }

    private void getNodeData(List<String> nodes){
        for(String node:nodes){
            String address = client.readData(PATH+"/"+node);
            addressList.add(address);
        }
    }
}
