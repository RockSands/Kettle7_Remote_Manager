package com.kettle;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 上下文创建完成后执行的事件监听器 spring boot上下文context创建完成，但此时spring中的bean是没有完全加载完成的,此时没有日子可以输出
 */
public class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent> {

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		try {
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
					"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
		} catch (Exception e) {
			throw new RuntimeException("监听异常");
		}
	}
}
