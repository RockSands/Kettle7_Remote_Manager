package com.kettle;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 上下文创建完成后执行的事件监听器 spring boot上下文context创建完成，但此时spring中的bean是没有完全加载完成的
 */
public class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent> {
	// private Logger logger =
	// LoggerFactory.getLogger(ApplicationPreparedEventListener.class);

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		try {
			KettleEnvironment.init();
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
					"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
		} catch (KettleException e) {
			throw new RuntimeException("KettleEnvironment初始化失败!");
		}
	}
}
