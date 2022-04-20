package com.mashibing.propertyEditor;

import com.mashibing.MyClassPathXmlApplicationContext;

public class Test {

    public static void main(String[] args) {
        MyClassPathXmlApplicationContext ac = new MyClassPathXmlApplicationContext("propertyEditor.xml");
        Customer bean=(Customer)ac.getBean("customer");
//       System.out.println(bean);
//        ConversionService bean = ac.getBean(ConversionService.class);
//        Student convert = bean.convert("1_zhangsan", Student.class);
//        System.out.println(convert);







    }


}
