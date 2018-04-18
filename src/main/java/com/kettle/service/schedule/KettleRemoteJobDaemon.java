package com.kettle.service.schedule;

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
		List<KettleRecord> runningRecord = kettleRecordRepository.queryRunningRecordsByHostName(client.getHostName());
		KettleRecord indexRecord;
		// 处理运行中的
		for (Iterator<KettleRecord> it = runningRecord.iterator(); it.hasNext();) {
			try {
				indexRecord = it.next();
				client.remoteJobStatus(indexRecord);
				if (indexRecord.isFinished()) {
					kettleRecordRepository.updateRecord(indexRecord);
					client.remoteRemoveJobNE(indexRecord);
					it.remove();
				} else if (indexRecord.isRunning()) {
					continue;
				} else {
					kettleRecordRepository.updateRecord(indexRecord);
					it.remove();
				}
			} catch (KettleException e) {
				e.printStackTrace();
			}
		}
		// 申请未运行的
		int size = kettleRecordProperties.getMaxPreRemote() - runningRecord.size();
		List<KettleRecord> readyRecords = kettleRecordPool.next(size, client.getHostName());
		for (KettleRecord kettleRecord : readyRecords) {
			try {
				client.remoteStartJob(kettleRecord);
				kettleRecord.setStatus(KettleVariables.RECORD_STATUS_RUNNING);
			} catch (KettleException e) {
				kettleRecord.setStatus(KettleVariables.RECORD_STATUS_ERROR);
			}
			kettleRecordRepository.updateRecord(kettleRecord);
		}
	}
}
