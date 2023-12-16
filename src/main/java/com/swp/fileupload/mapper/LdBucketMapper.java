package com.swp.fileupload.mapper;

import com.swp.fileupload.model.LdBucket;

public interface LdBucketMapper {

    int deleteByPrimaryKey(String id);

    int insert(LdBucket record);

    int insertSelective(LdBucket record);

    LdBucket selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(LdBucket record);

    int updateByPrimaryKey(LdBucket record);

    /**
     * @param bucketName
     * @return
     */
    LdBucket selectByName(String bucketName);
}
