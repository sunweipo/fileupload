package com.swp.fileupload.service;


import com.swp.fileupload.model.LdBucket;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月20日 下午4:18:01 $
 */
public interface LdBucketService {

    /**
     * 用ak,sk,bucket校验参数是否合法
     *
     * @param ak
     * @param sk
     * @param bucket
     * @return
     */
    boolean checkAuth(String ak, String sk, String bucket);

    /**
     * 根据bucket名字获取id
     *
     * @param bucket
     * @return
     */
    String getBucketIdByName(String bucket);

    /**
     * 返回保存分块的临时路径
     *
     * @param bucket
     * @param today
     * @return
     */
    String getTempPath(String bucket, String today);

    /**
     * 根据bucketname获取bucket对象
     *
     * @param bucket
     * @return
     */
    LdBucket getBucketByName(String bucket);

    /**
     * @param bucket
     * @return
     */
    String getPath(String bucket);

    void addBucket(LdBucket bucket);

    void clearCache();
}
