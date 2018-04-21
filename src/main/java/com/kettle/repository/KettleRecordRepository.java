package com.kettle.repository;

import java.util.Date;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kettle.mapper.KettleRecordMapper;
import com.kettle.model.record.KettleRecord;
import com.kettle.model.record.KettleRecordRelation;

@Component
public class KettleRecordRepository {
	@Autowired
	private KettleRecordMapper kettleRecordMapper;

	/**
	 * 获取一个
	 * 
	 * @param uuid
	 * @return
	 * @throws KettleException
	 */
	@Transactional(readOnly = true)
	public KettleRecord queryRecord(String uuid) {
		return kettleRecordMapper.queryRecord(uuid);
	}

	/**
	 * 补充依赖
	 * 
	 * @param record
	 * @return
	 */
	@Transactional(readOnly = true)
	public KettleRecord queryRecordRelations(KettleRecord record) {
		List<KettleRecordRelation> kettleRecordRelations = kettleRecordMapper.queryRecordRelations(record.getUuid());
		record.getRelations().clear();
		record.getRelations().addAll(kettleRecordRelations);
		return record;
	}

	/**
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<KettleRecord> allUnassignedRecords() {
		return kettleRecordMapper.allUnassignedRecords();
	}

	/**
	 * 获取Cline端的运行中任务
	 * 
	 * @param hostName
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<KettleRecord> queryRunningRecordsByHostName(String hostName) {
		return kettleRecordMapper.queryRunningRecordsByHostName(hostName);
	}

	@Transactional(readOnly = true)
	public List<KettleRecord> queryApplyRecordsByHostName(String hostName) {
		return kettleRecordMapper.queryApplyRecordsByHostName(hostName);
	}

	/**
	 * 保存
	 * 
	 * @param record
	 */
	@Transactional()
	public void insertRecord(KettleRecord record) {
		Date now = new Date();
		record.setCreateTime(now);
		record.setUpdateTime(now);
		for (KettleRecordRelation recordRelation : record.getRelations()) {
			recordRelation.setCreateTime(now);
		}
		kettleRecordMapper.insertRecord(record);
		kettleRecordMapper.insertRecordRelations(record.getRelations());
	}

	/**
	 * 更新非状态
	 * 
	 * @param record
	 */
	@Transactional()
	public int updateRecord(KettleRecord record) {
		record.setUpdateTime(new Date());
		return kettleRecordMapper.updateRecord(record);
	}

	/**
	 * 删除Record
	 * 
	 * @param record
	 */
	@Transactional()
	public void deleteRecord(String uuid) {
		kettleRecordMapper.deleteRecord(uuid);
		kettleRecordMapper.deleteRecordRelations(uuid);
	}
}
