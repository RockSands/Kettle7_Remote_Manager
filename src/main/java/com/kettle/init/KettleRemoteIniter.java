package com.kettle.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class KettleRemoteIniter implements ApplicationRunner {
	private Logger logger = LoggerFactory.getLogger(KettleRemoteIniter.class);

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("KettleRemoteIniter....");
	}

}
