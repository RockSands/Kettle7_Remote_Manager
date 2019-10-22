package com.kettle.core.record;

import java.util.ArrayList;
import java.util.List;

import com.kettle.model.record.KettleRecord;
import com.kettle.repository.KettleRecordRepository;

public class KettleRecordPool {

	private KettleRecordRepository kettleRecordRepository;
	
	public List<KettleRecord> next(int size, String hostName) {
		List<KettleRecord> records = kettleRecordRepository.allUnassignedRecords();
		List<KettleRecord> applyRecords = new ArrayList<KettleRecord>(size);
		for (KettleRecord record : records) {
			if (kettleRecordRepository.assignedRecord(record.getUuid(), hostName) == 1) {
				record.setHostname(hostName);
				applyRecords.add(record);
				if (applyRecords.size() == size) {
					return applyRecords;
				}
			}
		}
		return applyRecords;
	}
}
