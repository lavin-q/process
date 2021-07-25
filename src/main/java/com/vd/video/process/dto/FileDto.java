package com.vd.video.process.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 文件信息传输实体
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class FileDto implements Serializable {

    /*
     * 文件名
     */
    @ApiModelProperty("文件组名,由于可能为文件组")
    @NotBlank(message = "文件名不能为空")
    private String fileGroupName;

    /**
     * 文件类型(1：视频，2：图片，3：ppt)
     */
    @ApiModelProperty("文件类型：1.视频，2.图片，3.PPT")
    @NotNull(message = "文件类型不为空")
    @Range(min = 1, max = 3, message = "文件类型必须大于1小于3")
    private Integer fileType;

    /**
     * 封面地址
     */
    @ApiModelProperty("文件封面地址")
    //@NotBlank(message = "文件封面路径不能为空")
    private String fileCoverPath;

    /**
     * 文件列表
     */
    @ApiModelProperty("文件列表")
    @NotEmpty(message = "文件列表不可为空")
    private List<VideoDto> videoList;

}
