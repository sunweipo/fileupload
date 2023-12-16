package com.swp.fileupload.mapper;

import com.swp.fileupload.model.LdStorageConfig;

import java.util.List;

public interface LdStorageConfigMapper {
    int deleteByPrimaryKey(String id);

    int insert(LdStorageConfig record);

    int insertSelective(LdStorageConfig record);

    LdStorageConfig selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(LdStorageConfig record);

    int updateByPrimaryKey(LdStorageConfig record);

    /**
     * @param bucketId
     * @return
     */
    List<LdStorageConfig> selectByBucketId(String bucketId);
}
