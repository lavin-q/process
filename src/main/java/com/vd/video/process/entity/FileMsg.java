package com.vd.video.process.entity;

import java.io.Serializable;

public class FileMsg implements Serializable {

    private Long fileId;

    private String fileGroupName;

    private Integer fileType;

    private String fileCoverPath;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileGroupName() {
        return fileGroupName;
    }

    public void setFileGroupName(String fileGroupName) {
        this.fileGroupName = fileGroupName == null ? null : fileGroupName.trim();
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public String getFileCoverPath() {
        return fileCoverPath;
    }

    public void setFileCoverPath(String fileCoverPath) {
        this.fileCoverPath = fileCoverPath == null ? null : fileCoverPath.trim();
    }
}