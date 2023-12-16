package com.swp.fileupload.service.impl;

import com.swp.fileupload.mapper.LdFileRecordMapper;
import com.swp.fileupload.model.LdFileRecord;
import com.swp.fileupload.service.LdFileRecordService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月20日 下午4:18:34 $
 */
@Service
public class LdFileRecordServiceImpl implements LdFileRecordService {
    @Resource
    LdFileRecordMapper ldFileRecordMapper;

    @Override
    public List<LdFileRecord> getFileByMd5AndName(String fileName, @Param("fileMd5") String fileMd5) {
        return ldFileRecordMapper.selectByMd5AndName(fileName, fileMd5);

    }

    @Override
    public void addLdFileRecord(LdFileRecord record) {
        ldFileRecordMapper.insert(record);
    }
}
