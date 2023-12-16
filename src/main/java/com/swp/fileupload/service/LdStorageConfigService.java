package com.swp.fileupload.service;



import com.swp.fileupload.model.LdStorageConfig;

import java.util.List;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月20日 下午4:18:01 $
 */
public interface LdStorageConfigService {

    /**
     * @param bucketId
     * @return
     */
    List<LdStorageConfig> getConfigByBucketId(String bucketId);

}
