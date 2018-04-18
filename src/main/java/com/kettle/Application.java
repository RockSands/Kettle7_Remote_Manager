package com.kettle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.kettle.config.KettleRecordProperties;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@EnableScheduling
@EnableTransactionManagement
public class Application extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(Application.class);
		app.addListeners(new ApplicationEnvironmentPreparedEventListener());
		app.addListeners(new ApplicationFailedEventListener());
		app.addListeners(new ApplicationPreparedEventListener());
		app.addListeners(new ApplicationStartedEventListener());
		ConfigurableApplicationContext context = app.run(args);
		KettleRecordProperties properties = context.getBean(KettleRecordProperties.class);
		System.out.println(properties);
	}
}
