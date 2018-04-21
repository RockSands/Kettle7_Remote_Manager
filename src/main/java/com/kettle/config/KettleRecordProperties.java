package com.kettle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:/config/kettle_env.properties")
public class KettleRecordProperties {

	@Value("${kettle.record.maxPreRemote}")
	private int maxPreRemote;

	@Value("${kettle.record.run.timeout}")
	private int runTimeout;
	
	@Value("${kettle.record.once.save.period}")
	private int onceRecordSavePeriod;

	public int getMaxPreRemote() {
		return maxPreRemote;
	}

	public void setMaxPreRemote(int maxPreRemote) {
		this.maxPreRemote = maxPreRemote;
	}

	public int getRunTimeout() {
		return runTimeout;
	}

	public void setRunTimeout(int runTimeout) {
		this.runTimeout = runTimeout;
	}

	public int getOnceRecordSavePeriod() {
		return onceRecordSavePeriod;
	}

	public void setOnceRecordSavePeriod(int onceRecordSavePeriod) {
		this.onceRecordSavePeriod = onceRecordSavePeriod;
	}
}
