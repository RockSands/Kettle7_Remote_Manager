package com.kettle.core.remote;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.kettle.consist.KettleVariables;
import com.kettle.repository.KettleRepoRepository;

/**
 * Kettle远程池,仅维护远端的状态
 * 
 * @author Administrator
 *
 */
public class KettleRemotePool {
	/**
	 * 日志
	 */
	private static Logger logger = LoggerFactory.getLogger(KettleRemotePool.class);

	/**
	 * 远程连接
	 */
	private final Map<String, KettleRemoteClient> remoteclients = new ConcurrentHashMap<String, KettleRemoteClient>();

	/**
	 * 设备名称
	 */
	private final List<String> hostNames = new LinkedList<String>();

	/**
	 * Kettle资源
	 */
	private final KettleRepoRepository kettleRepoRepository;

	private String poolStatus;

	public KettleRemotePool(KettleRepoRepository kettleRepoRepository) throws KettleException {
		this.kettleRepoRepository = kettleRepoRepository;
		for (SlaveServer server : kettleRepoRepository.getSlaveServers()) {
			server.getLogChannel().setLogLevel(LogLevel.ERROR);
			addRemoteClient(new KettleRemoteClient(kettleRepoRepository, server));
			hostNames.add(server.getHostname());
		}
		logger.info("Kettle远程池已加载Client" + remoteclients.keySet());
	}

	@PostConstruct
	public void init() {
		refreshRemoteStatus();
	}

	/**
	 * 更新Client的定义
	 */
	@Scheduled(initialDelay = 10, fixedRate = 10L * 60L * 1000L)
	public void refreshRemoteDefine() {
		try {
			for (SlaveServer server : kettleRepoRepository.getSlaveServers()) {
				if (!hostNames.contains(server.getHostname())) {
					server.getLogChannel().setLogLevel(LogLevel.ERROR);
					addRemoteClient(new KettleRemoteClient(kettleRepoRepository, server));
					hostNames.add(server.getHostname());
				}
			}
		} catch (Exception e) {
			logger.error("Kettle远程池刷新失败!", e);
		}
	}

	/**
	 * 刷新远端状态
	 */
	private void refreshRemoteStatus() {
		boolean isRunning = false;
		for (KettleRemoteClient client : remoteclients.values()) {
			client.refreshStatus();
			if (client.isRunning()) {
				isRunning = true;
			}
		}
		if (!isRunning) {
			poolStatus = KettleVariables.REMOTE_STATUS_ERROR;
			logger.error("没有可用的远程的Kettle服务器运作!!!");
		} else {
			poolStatus = KettleVariables.REMOTE_STATUS_RUNNING;
		}
	}

	/**
	 * 定时刷新Client状态
	 */
	@Scheduled(initialDelay = 10000, fixedRate = 10000)
	public void refreshRemoteStatusScheduled() {
		refreshRemoteStatus();
	}

	/**
	 * 添加Kettle远端
	 * 
	 * @param remoteClient
	 */
	private void addRemoteClient(KettleRemoteClient remoteClient) {
		if (remoteclients.containsKey(remoteClient.getHostName())) {
			logger.error("Kettle的远程池添加Client[" + remoteClient.getHostName() + "]失败,该主机已存在!");
		} else {
			remoteclients.put(remoteClient.getHostName(), remoteClient);
			logger.info("Kettle的远程池添加Client[" + remoteClient.getHostName() + "]成功!");
		}
	}

	/**
	 * 获取所有Client
	 * 
	 * @return
	 */
	public Collection<KettleRemoteClient> getRemoteclients() {
		return remoteclients.values();
	}

	/**
	 * 获取指定Client
	 * 
	 * @return
	 */
	public KettleRemoteClient getRemoteclient(String hostname) {
		return remoteclients.get(hostname);
	}

	/**
	 * 是否运行中
	 * @return
	 */
	public boolean isRunning() {
		return KettleVariables.REMOTE_STATUS_RUNNING.equals(poolStatus);
	}
}
