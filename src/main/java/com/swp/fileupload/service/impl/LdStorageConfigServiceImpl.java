package com.swp.fileupload.service.impl;

import com.swp.fileupload.mapper.LdStorageConfigMapper;
import com.swp.fileupload.model.LdStorageConfig;
import com.swp.fileupload.service.LdStorageConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月20日 下午4:18:34 $
 */
@Service
public class LdStorageConfigServiceImpl implements LdStorageConfigService {
    @Resource
    LdStorageConfigMapper ldStorageConfigMapper;

    @Override
    public List<LdStorageConfig> getConfigByBucketId(String bucketId) {
        return ldStorageConfigMapper.selectByBucketId(bucketId);
    }
}
