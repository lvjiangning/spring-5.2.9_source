package com.mashibing.factoryBean;

import org.springframework.beans.factory.FactoryBean;

/**
 * MyFactoryBean会交由spring进行管理
 * 1、通过getBean类型 MyFactoryBean.class获取
 * 2、通过getBean属性$myFactoryBean
 */
public class MyFactoryBean implements FactoryBean<User> {
    /**
     * 不会在beanFactory的过程中创建，而是通过容器getbean时才会调用生成，spring是否会管理，通过isSingleton()返回值控制
     * @return
     * @throws Exception
     */
    @Override
    public User getObject() throws Exception {
        //任何创建对象的操作
        return new User("zhangsan");
    }

    @Override
    public Class<?> getObjectType() {
        return User.class;
    }

    /**
     *
     * @return true  创建后缓存在factoryBeanObjectCache中中
     *         false  每次都创建一个新的对象
     */
    @Override
    public boolean isSingleton() {
        return false;
    }
}
