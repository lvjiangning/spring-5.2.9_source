package com.mashibing.componentScan.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@ComponentScan("com.mashibing.namespaceHandler")
public class MyComponentScan {

    @ComponentScan("com.mashibing.namespaceHandler")
    @Configuration
    @Order(90)
    class InnerClass{

    }

}
