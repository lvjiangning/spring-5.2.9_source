package com.mashibing.populateBean;

import com.mashibing.populateBean.annotation.DependController;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestPopulate {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("populateBean.xml","factoryMethod.xml","propertyEditor.xml","methodOverride.xml");
        DependController dependController = (DependController)ac.getBean("dependController");
        ac.close();

    }
}
