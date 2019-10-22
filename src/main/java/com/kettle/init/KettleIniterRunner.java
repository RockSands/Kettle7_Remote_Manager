package com.kettle.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Kettle初始化 CommandLineRunner, SpringBoot初始化
 * 
 * @author Administrator
 *
 */
@Component
@Order(1)
public class KettleIniterRunner implements CommandLineRunner {
	private Logger logger = LoggerFactory.getLogger(KettleIniterRunner.class);

	@Override
	public void run(String... args) throws Exception {
		logger.info("===>KettleIniterRunner");
	}
}
