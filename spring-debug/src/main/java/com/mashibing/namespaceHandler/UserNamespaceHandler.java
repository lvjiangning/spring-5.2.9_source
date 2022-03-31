package com.mashibing.namespaceHandler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 自定义UserNamespaceHandler
 */
public class UserNamespaceHandler extends NamespaceHandlerSupport {
    /**
     * org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver#resolve(java.lang.String)
     */
    @Override
    public void init() {
        registerBeanDefinitionParser("user",new UserBeanDefinitionParser());
    }
}
