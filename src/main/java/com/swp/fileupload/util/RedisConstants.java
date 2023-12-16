package com.swp.fileupload.util;

/**
 * 存储redis相关的key，field
 *
 * @author yeqi
 * @version $Revision: 49274 $, $Date: 2021-07-16 17:41:24 +0800 (Fri, 16 Jul 2021) $
 */
public class RedisConstants {

    public static final int EXPIRE_ONE_MONTH = 30 * 24 * 60 * 60;

    public static final int EXPIRE_ONE_WEEK = 7 * 24 * 60 * 60;
    public static final int EXPIRE_ONE_DAY = 24 * 60 * 60;

    public static final int EXPIRE_SYSTEM_DATA = 10 * 365 * 24 * 60 * 60;

    public static final int EXPIRE_HALF_MONTH = 15 * 24 * 60 * 60;

    public static final int EXPIRE_HALF_HOUR = 30 * 60;
    public static final int EXPIRE_ONE_HOUR = 60 * 60;
    public static final int EXPIRE_ONE_MINUTE = 60;

    public static final String KEY_FILE_UPLOAD_ID_OBJ_NAME = "file.upload.id.obj.name:%s";
    /**
     * 存蓝叮上传文件的uploadId失效时间
     */
    public static final String KEY_LD_FILE_UPLOAD_EXPIRE = "file.upload.expire:%s";

    /**
     * 存蓝叮上传文件分块的临时路径key:uploadId
     */
    public static final String KEY_LD_FILE_UPLOAD_FOLDER = "file.upload.folder:%s";
    /**
     * token和bucket的对应
     */
    public static final String KEY_TOKEN_BUCKET = "file.token.bucket:%s";

    /**
     * 单位的学年学期
     */
    public static final String KEY_SYNC_LDSCHOOL_UNIT_SEMESTER = "sync.ldschool.semester:%s";
    public static final String FIELD_SYNC_LDSCHOOL_UNIT_ACADYEAR = "acadyear";
    public static final String FIELD_SYNC_LDSCHOOL_UNIT_SEMESTER = "semester";

}
