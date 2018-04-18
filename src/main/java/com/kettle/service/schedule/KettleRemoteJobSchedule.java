package com.kettle.service.schedule;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.pentaho.di.core.exception.KettleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kettle.core.remote.KettleRemoteClient;
import com.kettle.core.remote.KettleRemotePool;
import com.kettle.model.record.KettleRecord;
import com.kettle.service.KettleJobService;
import com.kettle.utils.SpringContextUtils;

@Service
public class KettleRemoteJobSchedule extends KettleJobService {

	@Autowired
	private KettleRemotePool kettleRemotePool;

	private ExecutorService fixedThreadPool;

	private final Map<String, KettleRemoteJobDaemon> daemons = new HashMap<String, KettleRemoteJobDaemon>();

	@PostConstruct
	public void init() throws KettleException {
		fixedThreadPool = Executors.newFixedThreadPool(2);

	}

	@Scheduled(initialDelay = 1000, fixedRate = 2000)
	public void schedule() {
		for (KettleRemoteClient client : kettleRemotePool.getRemoteclients()) {
			if (daemons.containsKey(client.getHostName())) {
				daemons.put(client.getHostName(), SpringContextUtils.getBean(KettleRemoteJobDaemon.class, client));
			}
			fixedThreadPool.execute(daemons.get(client.getHostName()));
		}
	}

	@Override
	protected void jobMustDie(KettleRecord record) throws KettleException {
		// TODO Auto-generated method stub

	}
}
