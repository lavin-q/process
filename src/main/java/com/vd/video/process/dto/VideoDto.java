package com.vd.video.process.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 视频传输实体
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class VideoDto implements Serializable {

    @ApiModelProperty("视频名称(将所有文件都当成一个视频)")
    @NotBlank(message = "视频名不可为空")
    private String fileName;

    @ApiModelProperty("视频上传路径")
    @NotBlank(message = "视频路径不可为空")
    private String filePath;
}
