package com.kettle.service.schedule;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kettle.config.KettleRecordProperties;
import com.kettle.consist.KettleVariables;
import com.kettle.model.record.KettleRecord;
import com.kettle.repository.KettleRecordRepository;
import com.kettle.repository.KettleRepoRepository;

/**
 * 每天凌晨处理任务
 * 
 * @author Administrator
 *
 */
@Service
public class KettleJobCleanTask {
	@Autowired
	private KettleRecordProperties kettleRecordProperties;
	@Autowired
	private KettleRecordRepository kettleRecordRepository;
	@Autowired
	private KettleRepoRepository kettleRepoRepository;

	/**
	 * 每小时执行一次 仅清理Once执行类型的任务
	 */
	//@Scheduled(cron = "0 0 0/1 * * ?")
	@Scheduled(cron = "0 0/1 * * * ?")
	public void AutoCleanJob() {
		try {
			/*
			 * 清理任务
			 */
			List<KettleRecord> records = kettleRecordRepository.allCanDelRecords();
			Long current = System.currentTimeMillis();
			for (KettleRecord record : records) {
				if ((current - record.getUpdateTime().getTime()) / 3600000L > kettleRecordProperties
						.getOnceRecordSavePeriod()) {
					kettleRecordRepository.queryRecordRelations(record);
					kettleRepoRepository.deleteJobEntireDefine(record);
					kettleRecordRepository.deleteRecord(record.getUuid());
				}
			}
			/*
			 * 清理目录
			 */
			kettleRepoRepository.deleteEmptyRepoPath(Arrays.asList(KettleVariables.RECORD_EXECUTION_TYPE_PERSISTENT,
					KettleVariables.RECORD_EXECUTION_TYPE_CRON));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
