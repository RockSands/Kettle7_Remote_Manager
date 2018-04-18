package com.kettle;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * spring boot 配置环境事件监听
 * 在该监听中获取到ConfigurableEnvironment后可以对配置信息做操作，例如：修改默认的配置信息，增加额外的配置信息
 */
public class ApplicationEnvironmentPreparedEventListener
		implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
	// private Logger logger =
	// LoggerFactory.getLogger(ApplicationEnvironmentPreparedEventListener.class);

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
	}
}
