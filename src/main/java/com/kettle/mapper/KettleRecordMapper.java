package com.kettle.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kettle.model.record.KettleRecord;
import com.kettle.model.record.KettleRecordRelation;

@Mapper
public interface KettleRecordMapper {
    KettleRecord queryRecord(@Param("uuid") String uuid);

    List<KettleRecordRelation> queryRecordRelations(@Param("masterUuid") String masterUuid);
    
    List<KettleRecord> allUnassignedRecords();
    
    List<KettleRecord> queryRunningRecordsByHostName(@Param("hostname")String hostname);

    int insertRecord(KettleRecord record);

    int insertRecordRelations(@Param("list") List<KettleRecordRelation> recordRelations);

    int updateRecord(KettleRecord record);

    int updateRecordRelationID(@Param("newID") String newID, @Param("createTime") Date createTime,
	    @Param("oldID") String oldID);

    int deleteRecord(@Param("uuid") String uuid);

    int deleteRecordRelations(@Param("masterUUID") String masterUUID);

	
}
