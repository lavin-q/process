package com.vd.video.process.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: qihumao
 * @Description:
 * @Date: 2021-07-02 11:26
 * @Version: 1.0.0
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class FileVideoVo implements Serializable {

    /**
     * 文件ID
     */
    @ApiModelProperty("文件ID")
    private Long fileId;

    /**
     * 文件名
     */
    @ApiModelProperty("文件名称")
    private String fileName;

    /**
     * 文件类型
     */
    @ApiModelProperty("文件类型：1.视频，2.图片，3.PPT")
    private Integer fileType;

    /**
     * 文件封面地址 /file/xxxx
     */
    @ApiModelProperty(" 文件封面地址")
    private String fileCoverPath;

    /**
     * 文件类表
     */
    @ApiModelProperty("文件列表")
    private List<VideoVo> fileList;

    /**
     * 播放状态(0.未播放,1.正在播放,2.暂停)
     */
    @ApiModelProperty("播放状态(0.未播放,1.正在播放,2.暂停)")
    private Integer playStatus = 0;
}


