package com.swp.fileupload.enums;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月23日 下午5:52:26 $
 */
public enum LogEnum {
    BUSSINESS("bussiness"), PLATFORM("platform"), DB("db"), EXCEPTION("exception");

    private String category;

    LogEnum(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
