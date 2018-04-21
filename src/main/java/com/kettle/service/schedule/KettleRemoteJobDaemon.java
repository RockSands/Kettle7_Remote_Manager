package com.kettle.service.schedule;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.kettle.config.KettleRecordProperties;
import com.kettle.consist.KettleVariables;
import com.kettle.core.record.KettleRecordPool;
import com.kettle.core.remote.KettleRemoteClient;
import com.kettle.model.record.KettleRecord;
import com.kettle.repository.KettleRecordRepository;

@Component
@Lazy
public class KettleRemoteJobDaemon extends Thread {

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
			}
			return;
		}
		int size = kettleRecordProperties.getMaxPreRemote() - runningRecords.size() - applyRecords.size();
		if(size > 0) {
			applyRecords.addAll(kettleRecordPool.next(size, client.getHostName()));
		}
		for (KettleRecord kettleRecord : applyRecords) {
			try {
				client.remoteSendJob(kettleRecord);
				kettleRecord.setStatus(KettleVariables.RECORD_STATUS_RUNNING);
			} catch (KettleException e) {
				kettleRecord.setStatus(KettleVariables.RECORD_STATUS_ERROR);
			}
			kettleRecordRepository.updateRecord(kettleRecord);
		}
	}
}
