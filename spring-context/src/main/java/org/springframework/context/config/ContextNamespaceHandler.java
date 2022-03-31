/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.context.annotation.AnnotationConfigBeanDefinitionParser;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;

/**
 * {@link org.springframework.beans.factory.xml.NamespaceHandler}
 * for the '{@code context}' namespace.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 */
public class ContextNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("property-placeholder", new PropertyPlaceholderBeanDefinitionParser());
//		https://cloud.tencent.com/developer/article/1890784
		//属性文件指定的信息可以直接覆盖spring xml配置文件的元数据
		registerBeanDefinitionParser("property-override", new PropertyOverrideBeanDefinitionParser());
//		https://www.cnblogs.com/zhangsonglin/p/11181064.html
		registerBeanDefinitionParser("annotation-config", new AnnotationConfigBeanDefinitionParser());
		registerBeanDefinitionParser("component-scan", new ComponentScanBeanDefinitionParser());
//		https://blog.csdn.net/LeoHan163/article/details/106921570/
		registerBeanDefinitionParser("load-time-weaver", new LoadTimeWeaverBeanDefinitionParser());
//		https://blog.csdn.net/why2sky/article/details/41977735
		//spring可以为ioc容器进行依赖注入：但某些类没有配置在ioc中，也可以进行依赖注入。
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
		//对于一个普通的java类，作为MBean 需要被管理，通过注解指定需要暴露的属性和方法。
		registerBeanDefinitionParser("mbean-export", new MBeanExportBeanDefinitionParser());
		//使用jms技术，资源被一种叫做Mbeans监控，这些Mbean都在核心对象管理的server上注册
		registerBeanDefinitionParser("mbean-server", new MBeanServerBeanDefinitionParser());
	}

}
