package com.kettle.service.schedule;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kettle.config.KettleRecordProperties;
import com.kettle.consist.KettleVariables;
import com.kettle.core.record.KettleRecordPool;
import com.kettle.core.remote.KettleRemoteClient;
import com.kettle.model.record.KettleRecord;
import com.kettle.repository.KettleRecordRepository;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class KettleRemoteJobDaemon extends Thread {

	private static Logger logger = LoggerFactory.getLogger(KettleRemoteJobDaemon.class);

	private final KettleRemoteClient client;

	@Autowired
	private KettleRecordRepository kettleRecordRepository;

	@Autowired
	private KettleRecordProperties kettleRecordProperties;

	@Autowired
	private KettleRecordPool kettleRecordPool;

	public KettleRemoteJobDaemon(KettleRemoteClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		logger.debug("Kettle-Client[" + client.getHostName() + "]任务管理启动!");
		List<KettleRecord> runningRecords = kettleRecordRepository.queryRunningRecordsByHostName(client.getHostName());
		KettleRecord recordRoll;
		Date now = new Date();
		if (!client.isRunning()) {
			// 网络连接失败!
			for (Iterator<KettleRecord> it = runningRecords.iterator(); it.hasNext();) {
				recordRoll = it.next();
				recordRoll.setStatus(KettleVariables.RECORD_STATUS_ERROR);
				recordRoll.setUpdateTime(now);
				kettleRecordRepository.updateRecord(recordRoll);
				logger.debug(
						"Kettle-Client[" + client.getHostName() + "]任务管理由于远端无法连接,失败任务[" + recordRoll.getUuid() + "]!");
			}
			return;
		}
		// 处理运行中的
		for (Iterator<KettleRecord> it = runningRecords.iterator(); it.hasNext();) {
			recordRoll = it.next();
			try {
				client.remoteJobStatus(recordRoll);
				if (recordRoll.isFinished()) {
					kettleRecordRepository.updateRecord(recordRoll);
					client.remoteRemoveJobNE(recordRoll);
					it.remove();
				} else if (recordRoll.isRunning()) {
					if (kettleRecordProperties.getRunTimeout() > 0
							&& ((System.currentTimeMillis() - recordRoll.getUpdateTime().getTime())
									/ 60000L) > kettleRecordProperties.getRunTimeout()) {
						recordRoll.setStatus(KettleVariables.RECORD_STATUS_ERROR);
						recordRoll.setErrMsg("Kettle的任务[" + recordRoll.getUuid() + "]执行超时!");
						kettleRecordRepository.updateRecord(recordRoll);
						client.remoteRemoveJobNE(recordRoll);
						it.remove();
						logger.debug("Kettle-Client[" + client.getHostName() + "]任务管理信息,超时任务[" + recordRoll.getUuid()
								+ "]!");
					}
				} else {
					kettleRecordRepository.updateRecord(recordRoll);
					it.remove();
				}
			} catch (KettleException e) {
				recordRoll.setStatus(KettleVariables.RECORD_STATUS_ERROR);
				recordRoll.setErrMsg(e.getMessage());
				kettleRecordRepository.updateRecord(recordRoll);
				it.remove();
				logger.error("Kettle-Client[" + client.getHostName() + "]任务管理信息,异常任务[" + recordRoll.getUuid() + "]!",
						e);
			}
		}
		// 申请未运行的
		List<KettleRecord> applyRecords = kettleRecordRepository.queryApplyRecordsByHostName(client.getHostName());
		if (!client.isRunning()) {// 如果未运行,将改所属Apply任务释放
			KettleRecord update = null;
			for (KettleRecord record : applyRecords) {
				update = new KettleRecord();
				update.setHostname(null);
				update.setUuid(record.getUuid());
				kettleRecordRepository.updateRecord(update);
				logger.debug("Kettle-Client[" + client.getHostName() + "]由于远端未连接,退回任务[" + update.getUuid() + "]!");
			}
			return;
		}
		int size = kettleRecordProperties.getMaxPreRemote() - runningRecords.size() - applyRecords.size();
		if (size > 0) {
			applyRecords.addAll(kettleRecordPool.next(size, client.getHostName()));
		}
		for (KettleRecord kettleRecord : applyRecords) {
			try {
				client.remoteSendJob(kettleRecord);
				kettleRecord.setStatus(KettleVariables.RECORD_STATUS_RUNNING);
				logger.debug("Kettle-Client[" + client.getHostName() + "]任务管理信息,启动任务[" + kettleRecord.getUuid() + "]!");
			} catch (KettleException e) {
				kettleRecord.setStatus(KettleVariables.RECORD_STATUS_ERROR);
				logger.debug("Kettle-Client[" + client.getHostName() + "]任务管理信息,异常任务[" + kettleRecord.getUuid() + "]!",
						e);
			}
			kettleRecordRepository.updateRecord(kettleRecord);
		}
	}
}
