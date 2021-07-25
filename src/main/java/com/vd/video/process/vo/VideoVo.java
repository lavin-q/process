package com.vd.video.process.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class VideoVo implements Serializable {

    @ApiModelProperty("视频ID")
    private Long videoId;

    @ApiModelProperty("视频路径")
    private String videoPath;

    @ApiModelProperty("视频名称")
    private String videoName;

}