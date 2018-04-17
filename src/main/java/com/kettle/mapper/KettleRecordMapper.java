package com.kettle.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kettle.model.KettleRecord;
import com.kettle.model.KettleRecordRelation;

@Mapper
public interface KettleRecordMapper {
    KettleRecord queryRecord(@Param("uuid") String uuid);

    List<KettleRecord> allWaitingRecords();

    List<KettleRecord> allStopRecords();

    List<KettleRecordRelation> queryRecordRelations(@Param("masterUuid") String masterUuid);

    void insertRecord(KettleRecord record);

    void insertRecordRelations(@Param("list") List<KettleRecordRelation> recordRelations);

    void updateRecord(KettleRecord record);

    void updateRecordRelationID(@Param("newID") String newID, @Param("createTime") Date createTime,
	    @Param("oldID") String oldID);

    void deleteRecord(@Param("uuid") String uuid);

    void deleteRecordRelations(@Param("masterUUID") String masterUUID);
}
