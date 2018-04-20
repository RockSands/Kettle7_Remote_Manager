package com.kettle.service.schedule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.pentaho.di.core.exception.KettleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static Logger logger = LoggerFactory.getLogger(KettleRemoteJobSchedule.class);

	@Autowired
	private KettleRemotePool kettleRemotePool;

	private ExecutorService fixedThreadPool;

	private final Map<String, KettleRemoteJobDaemon> daemons = new ConcurrentHashMap<String, KettleRemoteJobDaemon>();

	@Scheduled(initialDelay = 1000, fixedRate = 5000)
	public void schedule() {
		fixedThreadPool = Executors.newFixedThreadPool(kettleRemotePool.getRemoteclients().size());
		for (KettleRemoteClient client : kettleRemotePool.getRemoteclients()) {
			if (daemons.containsKey(client.getHostName())) {
				daemons.put(client.getHostName(), SpringContextUtils.getBean(KettleRemoteJobDaemon.class, client));
			}
			fixedThreadPool.execute(daemons.get(client.getHostName()));
		}
		fixedThreadPool.shutdown();
		try {
			fixedThreadPool.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			logger.error("线程池执行超时!", e);
		}
	}

	@Override
	protected void jobMustDie(KettleRecord record) throws KettleException {
		KettleRemoteClient client = kettleRemotePool.getRemoteclient(record.getHostname());
		if (client == null) {
			return;
		}
		client.remoteStopJobNE(record);
		client.remoteRemoveJobNE(record);
	}
}
