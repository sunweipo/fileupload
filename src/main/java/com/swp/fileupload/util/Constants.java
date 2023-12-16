package com.swp.fileupload.util;

import java.io.Serializable;

/**
 * 系统常量配置
 *
 * @author huwh
 * @version $Revision: 1.0 $, $Date: 2018年3月26日 下午8:40:57 $
 */
public class Constants implements Serializable {

    private static final long serialVersionUID = 4083993443618925465L;
    /**
     * 本服务文件上传的ak和sk
     */
    public static final String CONFIG_CODE_AK = "wanpengyun.accessKey";
    public static final String CONFIG_CODE_SK = "wanpengyun.apisecret";
    public static final String CONFIG_CODE_BUCKET = "wanpengyun.bucket";
    public static final String CONFIG_CODE_PATH = "wanpengyun.path";
    public static final int CODE_SUCCESS = 1;
    public static final int CODE_FAIL = 0;

}
