/*
接口扫描类
 */
package com.configurer;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import java.util.Arrays;
import java.util.Set;

public class ClassPathRpcScanner extends ClassPathBeanDefinitionScanner {

    private RpcFactoryBean<?> rpcFactoryBean = new RpcFactoryBean<Object>();

    public ClassPathRpcScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);//BeanDefinitionHolder为bean定义的持有者

        if (!beanDefinitions.isEmpty()) {
            processBeanDefinitions(beanDefinitions);
        } else {
            System.out.println("No RPC mapper was found in '" + Arrays.toString(basePackages)+"'");
        }
        return beanDefinitions;
    }

    //根据bean列表来一一定义bean
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();

            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());
            //注意，这里的BeanClass是生成Bean实例的工厂，不是Bean本身。
            // FactoryBean是一种特殊的Bean，其返回的对象不是指定类的一个实例，
            definition.setBeanClass(this.rpcFactoryBean.getClass());
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }

    public void registerFilters() {
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
                return true;
            }
        });
    }
}
