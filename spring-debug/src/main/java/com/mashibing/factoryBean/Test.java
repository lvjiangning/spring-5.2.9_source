package com.mashibing.factoryBean;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("factoryBean.xml");
        System.out.println("=========");
         MyFactoryBean bean1 = (MyFactoryBean) ac.getBean( "&myFactoryBean");
         System.out.println(bean1);
        User bean = (User) ac.getBean("myFactoryBean");
        System.out.println(bean);
        User bean2 = (User) ac.getBean("myFactoryBean");
        System.out.println(bean2);

    }

}
