package com.mashibing.cycle;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 创建对象的时，会先存放三级缓存：key=name ,value= lambda表达式，再存放在二级缓存中，获取循环依赖时，会先从二级缓存中取，如果存在则返回
 * 如果没有代理对象，二级缓存即可满足
 */
public class TestCycle {

    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("cycle.xml");
        A bean = ac.getBean(A.class);
        System.out.println(bean.getB());
        System.out.println(bean.getName());
        B bean1 = ac.getBean(B.class);
        System.out.println(bean1.getA());

    }
}
