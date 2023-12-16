package com.swp.fileupload.service;

import com.swp.fileupload.model.LdFileRecord;

import java.util.List;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月20日 下午4:18:01 $
 */
public interface LdFileRecordService {

    /**
     * 根据fileMd5和fileName获取文件列表
     *
     * @param fileMd5
     * @param fileName
     * @return
     */
    List<LdFileRecord> getFileByMd5AndName(String fileName, String fileMd5);

    /**
     * @param record
     */
    void addLdFileRecord(LdFileRecord record);

}
