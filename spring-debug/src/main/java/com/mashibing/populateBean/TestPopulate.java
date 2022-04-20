package com.mashibing.populateBean;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestPopulate {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("populateBean.xml","factoryMethod.xml","propertyEditor.xml","methodOverride.xml");
        ac.close();
    }
}
