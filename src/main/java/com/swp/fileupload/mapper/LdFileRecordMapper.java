package com.swp.fileupload.mapper;

import com.swp.fileupload.model.LdFileRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LdFileRecordMapper {
    int deleteByPrimaryKey(String id);

    int insert(LdFileRecord record);

    int insertSelective(LdFileRecord record);

    LdFileRecord selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(LdFileRecord record);

    int updateByPrimaryKey(LdFileRecord record);

    /**
     * @param fileName
     * @param fileMd5
     * @return
     */
    List<LdFileRecord> selectByMd5AndName(@Param("fileName") String fileName, @Param("fileMd5") String fileMd5);
}
