package com.swp.fileupload.model;

import java.util.Date;

public class LdFileRecord {
    private String id;

    private String bucket;

    private String fileName;

    private String url;

    private Integer isDeleted;

    private Date creationTime;

    private String fileMd5;

    private Integer size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket == null ? null : bucket.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5 == null ? null : fileMd5.trim();
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * @param id
     * @param bucket
     * @param fileName
     * @param url
     * @param isDeleted
     * @param creationTime
     * @param fileMd5
     * @param size
     */
    public LdFileRecord(String id, String bucket, String fileName, String url, Integer isDeleted, Date creationTime,
            String fileMd5, Integer size) {
        this.id = id;
        this.bucket = bucket;
        this.fileName = fileName;
        this.url = url;
        this.isDeleted = isDeleted;
        this.creationTime = creationTime;
        this.fileMd5 = fileMd5;
        this.size = size;
    }

    /**
     *
     */
    public LdFileRecord() {
    }

}
