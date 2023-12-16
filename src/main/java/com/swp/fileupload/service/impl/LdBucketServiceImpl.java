package com.swp.fileupload.service.impl;

import cn.hutool.core.lang.Validator;
import com.swp.fileupload.mapper.LdBucketMapper;
import com.swp.fileupload.model.LdBucket;
import com.swp.fileupload.model.LdStorageConfig;
import com.swp.fileupload.service.LdBucketService;
import com.swp.fileupload.service.LdStorageConfigService;
import com.swp.fileupload.util.StringUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月20日 下午4:18:34 $
 */
@Service
public class LdBucketServiceImpl implements LdBucketService {
    @Resource
    LdStorageConfigService ldStorageConfigService;
    @Resource
    LdBucketMapper ldBucketMapper;

    @Override
    public boolean checkAuth(String ak, String sk, String bucket) {
        String bucketId = getBucketIdByName(bucket);
        if (!Validator.isEmpty(bucketId)) {
            List<LdStorageConfig> configs = ldStorageConfigService.getConfigByBucketId(bucketId);
            for (LdStorageConfig config : configs) {
                if (ak.equals(config.getAccesskey()) && sk.equals(config.getApisecret())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param bucket
     */
    @Override
    public String getBucketIdByName(String bucket) {
        LdBucket ldBucket = getBucketByName(bucket);
        if (ldBucket != null) {
            return ldBucket.getId();
        }
        return null;
    }

    @Override
    public String getTempPath(String bucket, String today) {
        LdBucket ldBucket = getBucketByName(bucket);
        String tempPath = "/opt/data/" + bucket;
        if (ldBucket != null) {
            tempPath = ldBucket.getTempPath();
        }
        return StringUtil.mergeStrings(tempPath, "/temp", "/chunk/", today);
    }

    @Cacheable(cacheNames = "bucket", key = "#bucket")
    @Override
    public LdBucket getBucketByName(String bucket) {
        return ldBucketMapper.selectByName(bucket);
    }

    @Override
    public String getPath(String bucket) {
        String path = "";
        LdBucket ldBucket = getBucketByName(bucket);
        if (ldBucket != null) {
            path = ldBucket.getPath();
        }
        return path;
    }

    @Override
    public void addBucket(LdBucket bucket) {
        ldBucketMapper.insert(bucket);
    }

    @CacheEvict(cacheNames = "bucket", allEntries = true)
    @Override
    public void clearCache() {

    }

}
