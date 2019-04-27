package org.fkjava.weixin;

import org.fkjava.commons.EventListenerConfig;
import org.fkjava.commons.domain.event.EventInMessage;
import org.fkjava.commons.processors.EventMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.util.xml.StaxUtils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

// 实现ApplicationContextAware接口的目的：为了让当前对象能够得到Spring容器本身，能够通过Spring的容器来找到里面的Bean
@SpringBootApplication
@ComponentScan(basePackages = "org.fkjava")
@EnableJpaRepositories(basePackages = "org.fkjava")
@EntityScan(basePackages = "org.fkjava")
public class SubscribeApplication implements ApplicationContextAware
// 为了让非WEB应用能够一直等待信息的到来，必须实现CommandLineRunner接口
		, EventListenerConfig {

	private static final Logger LOG = LoggerFactory.getLogger(SubscribeApplication.class);
	private ApplicationContext ctx;

	// 这个方法，会在当前实例创建之后，由Spring自己调用，而Spring会把它本身传入进来
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ctx = applicationContext;
	}

	@Bean()
	public XmlMapper xmlMapper() {
		XmlMapper mapper = new XmlMapper(StaxUtils.createDefensiveInputFactory());
		return mapper;
	}

	@Override
	public void handleEvent(EventInMessage event) {
		LOG.trace("事件处理程序收到的消息：{}", event);
		String eventType = event.getEvent();// 获取事件类型
		eventType = eventType.toLowerCase();// 转换为小写

		// 调用消息的处理器，进行具体的消息处理
		String beanName = eventType + "MessageProcessor";
		EventMessageProcessor mp = (EventMessageProcessor) ctx.getBean(beanName);
		if (mp == null) {
			LOG.error("事件 {} 没有找到对应的处理器", eventType);
		} else {
			mp.onMessage(event);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(SubscribeApplication.class, args);
	}
}
