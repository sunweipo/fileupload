package com.swp.fileupload.dto;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class ContentResultDto implements Serializable {
    private static final long serialVersionUID = 5026129263128005352L;
    private int success;
    private String message;
    private JSONObject data;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

}
