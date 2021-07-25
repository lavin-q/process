package com.vd.video.process.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;


@Data
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class FileVo implements Serializable {
    private Long fileId;

    private String fileName;

    private Integer fileType;

    private String fileCoverPath;

    private Long videoId;

    private String videoName;

    private String videoPath;
}
