package com.swp.fileupload.model;

import java.util.Date;

public class LdBucket {
    private String id;

    private String name;

    private String path;

    private String tempPath;

    private String domain;

    private String app;

    private Integer isDeleted;

    private Date creationTime;

    private Date modifyTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath == null ? null : tempPath.trim();
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain == null ? null : domain.trim();
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app == null ? null : app.trim();
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

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * @param id
     * @param name
     * @param path
     * @param tempPath
     * @param domain
     * @param app
     * @param isDeleted
     * @param creationTime
     * @param modifyTime
     */
    public LdBucket(String id, String name, String path, String tempPath, String domain, String app, Integer isDeleted,
            Date creationTime, Date modifyTime) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.tempPath = tempPath;
        this.domain = domain;
        this.app = app;
        this.isDeleted = isDeleted;
        this.creationTime = creationTime;
        this.modifyTime = modifyTime;
    }

    /**
     *
     */
    public LdBucket() {
    }

}
