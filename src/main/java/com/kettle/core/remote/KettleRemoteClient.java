package com.kettle.core.remote;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.di.www.SlaveServerStatus;
import org.pentaho.di.www.WebResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.kettle.consist.KettleVariables;
import com.kettle.model.record.KettleRecord;
import com.kettle.repository.KettleRepoRepository;

/**
 * Kettle远程连接
 * 
 * @author chenkw
 *
 */
public class KettleRemoteClient {

	/**
	 * 日志
	 */
	private static Logger logger = LoggerFactory.getLogger(KettleRemoteClient.class);

	private final String hostName;

	/**
	 * 远端状态
	 */
	private String remoteStatus;

	/**
	 * 远程服务
	 */
	private final SlaveServer remoteServer;

	/**
	 * Kettle资源库
	 */
	@Autowired
	private final KettleRepoRepository kettleRepoRepository;

	public KettleRemoteClient(KettleRepoRepository kettleRepoRepository, SlaveServer remoteServer) {
		hostName = remoteServer.getHostname();
		this.kettleRepoRepository = kettleRepoRepository;
		this.remoteServer = remoteServer;
	}

	/**
	 * 是否运行状态
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return KettleVariables.REMOTE_STATUS_RUNNING.equals(remoteStatus);
	}

	public void refreshStatus() {
		try {
			SlaveServerStatus status = remoteServer.getStatus();
			if (!KettleVariables.REMOTE_STATUS_RUNNING.equals(remoteStatus)) {
				logger.error("Kettle远端[" + getHostName() + "]异常状态:" + status.getStatusDescription());
				remoteStatus = KettleVariables.REMOTE_STATUS_ERROR;
			} else {
				remoteStatus = KettleVariables.REMOTE_STATUS_RUNNING;
			}
		} catch (Exception e) {
			logger.error("Kettle远端[" + this.getHostName() + "]查询状态失败!", e);
		}
	}

	/**
	 * 远程推送Job
	 * 
	 * @param job
	 * @return
	 * @throws KettleException
	 */
	public String remoteSendJob(KettleRecord job) throws KettleException {
		JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
		jobExecutionConfiguration.setRemoteServer(remoteServer);
		jobExecutionConfiguration.setLogLevel(LogLevel.BASIC);
		jobExecutionConfiguration.setPassingExport(false);
		jobExecutionConfiguration.setExecutingRemotely(true);
		jobExecutionConfiguration.setExecutingLocally(false);
		jobExecutionConfiguration.setRepository(kettleRepoRepository.getRepository());
		String runID = null;
		JobMeta jobMeta = kettleRepoRepository.getMainJob(job);
		runID = Job.sendToSlaveServer(jobMeta, jobExecutionConfiguration, kettleRepoRepository.getRepository(),
				kettleRepoRepository.getRepository().getMetaStore());
		return runID;
	}

	/**
	 * 远程启动,
	 * 
	 * @param job
	 * @throws KettleException
	 */
	public void remoteStartJob(KettleRecord job) throws KettleException {
		WebResult result;
		try {
			result = remoteServer.startJob(job.getName(), job.getRunID());
		} catch (Exception e) {
			throw new KettleException("Kettle远端[" + this.getHostName() + "]启动Job[" + job.getUuid() + "]失败!", e);
		}
		if (!WebResult.STRING_OK.equals(result.getResult())) {
			throw new KettleException(
					"Kettle远端[" + this.getHostName() + "]启动Job[" + job.getUuid() + "]失败!\n" + result.getMessage());
		}
	}

	/**
	 * 远程停止任务
	 * 
	 * @param job
	 * @throws KettleException
	 */
	public void remoteStopJob(KettleRecord job) throws KettleException {
		WebResult result;
		try {
			result = remoteServer.stopJob(job.getName(), job.getRunID());
		} catch (Exception e) {
			throw new KettleException("Kettle远端[" + this.getHostName() + "]停止Job[" + job.getUuid() + "]失败!", e);
		}
		if (!WebResult.STRING_OK.equals(result.getResult())) {
			throw new KettleException(
					"Kettle远端[" + this.getHostName() + "]停止Job[" + job.getUuid() + "]失败!\n" + result.getMessage());
		}
	}

	/**
	 * 远程停止任务
	 * 
	 * @param job
	 */
	public void remoteStopJobNE(KettleRecord job) {
		try {
			remoteStopJob(job);
		} catch (Exception ex) {
			logger.debug("Kettle远端[" + this.getHostName() + "]强制停止Job[" + job.getUuid() + "]失败!", ex);
		}
	}

	/**
	 * 获取远端的状态
	 * 
	 * @param job
	 * @throws KettleException
	 */
	public void remoteJobStatus(KettleRecord job) throws KettleException {
		SlaveServerJobStatus jobStatus;
		try {
			jobStatus = remoteServer.getJobStatus(job.getName(), job.getRunID(), 0);
		} catch (Exception e) {
			throw new KettleException("Kettle远端[" + this.getHostName() + "]查询Job[" + job.getUuid() + "]失败!", e);
		}
		logger.debug("Kettle Remote[" + remoteServer.getHostname() + "]同步Job[" + job.getUuid() + "]状态为:"
				+ jobStatus.getStatusDescription());
		if (jobStatus == null || jobStatus.getStatusDescription() == null) {
			job.setStatus(KettleVariables.RECORD_STATUS_ERROR);
			job.setErrMsg("remote[" + this.getHostName() + "]未找到record[" + job.getUuid() + "]信息!");
		} else if (jobStatus.getStatusDescription().toUpperCase().contains("ERROR")) {
			job.setStatus(KettleVariables.RECORD_STATUS_ERROR);
			job.setErrMsg(jobStatus.getStatusDescription());
		} else if ("Finished".equalsIgnoreCase(jobStatus.getStatusDescription())) {
			job.setStatus(KettleVariables.RECORD_STATUS_FINISHED);
		} else {
			job.setStatus(KettleVariables.RECORD_STATUS_RUNNING);
		}
	}

	/**
	 * 清理运行的JOB
	 * 
	 * @param job
	 * @throws KettleException
	 */
	public void remoteRemoveJob(KettleRecord job) throws KettleException {
		WebResult result;
		try {
			result = remoteServer.removeJob(job.getName(), job.getRunID());
		} catch (Exception e) {
			throw new KettleException("Kettle远端[" + this.getHostName() + "]删除Job[" + job.getUuid() + "]失败!", e);
		}
		if (!WebResult.STRING_OK.equals(result.getResult())) {
			throw new KettleException(result.getMessage());
		}
	}

	/**
	 * 清理运行Job
	 * 
	 * @param job
	 */
	public void remoteRemoveJobNE(KettleRecord job) {
		try {
			remoteRemoveJob(job);
		} catch (Exception ex) {
			logger.error("Kettle远端[" + this.getHostName() + "]强制删除Job[" + job.getUuid() + "]失败!", ex);
		}

	}

	/**
	 * 获取HostName
	 * 
	 * @return
	 */
	public String getHostName() {
		return hostName;
	}
}
