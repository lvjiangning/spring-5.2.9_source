package com.mashibing.componentScan;

import com.mashibing.componentScan.shouldSkip.Person;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 调试component-scan包扫描
 */
public class Test {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("/componentScan/spring-componentScan.xml");
        Person bill = (Person)ac.getBean("bill");
        System.out.println(bill);
    }
}
