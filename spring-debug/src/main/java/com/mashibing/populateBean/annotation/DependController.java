package com.mashibing.populateBean.annotation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("db.properties")
public class DependController {
    @Value("${jdbc.username}")
    private String  username;

    //不使用${} 表示直接使用@value的值进行填充
    @Value("jdbc.password")
    private String  password;
}
