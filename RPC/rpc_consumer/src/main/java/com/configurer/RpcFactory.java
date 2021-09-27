package com.configurer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.entity.Request;
import com.entity.Response;
import com.netty.NettyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

//代理会自动调用该类的invoke方法(我们在这可以调用netty发送消息，并且接收到返回消息）
//该类为代理类
@Component
public class RpcFactory<T> implements InvocationHandler {

    @Autowired
    NettyClient client;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        request.setId((int) args[1]);

        System.out.println("请求数据为:"+request.toString());
        Response response = client.Send(request);
        return response.getData();
    }
}
