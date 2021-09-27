/*
定义代理类实现工厂
 */
package com.configurer;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

public class RpcFactoryBean<T> implements FactoryBean<T> {

    private Class<T> RpcInterface;

    @Autowired
    RpcFactory<T> factory;

    public RpcFactoryBean() {}

    public RpcFactoryBean(Class<T> RpcInterface) {
        this.RpcInterface = RpcInterface;
    }

    //该方法就是生产出实例的方法， 在我们的例子中，需要通过 InvocationHandler 产生的实例并返回。
    @Override
    public T getObject() throws Exception {
        /*
        Proxy.newProxyInstance()参数说明：
          loader: 用哪个类加载器去加载代理对象
          interfaces:动态代理类需要实现的接口
          h:动态代理方法在执行时，会调用h里面的invoke方法去执行
         */
        //完成相关配置，使RpcInterface这个接口在调用时，会去执行factory中的invoke方法。
        return (T) Proxy.newProxyInstance(RpcInterface.getClassLoader(), new Class[] { RpcInterface },factory);
    }

    //该方法负责向 IOC 容器解释此次产生的实现是注册到哪个类型上,这里指RpcInterface.class
    @Override
    public Class<?> getObjectType() {
        return this.RpcInterface;
    }
}

