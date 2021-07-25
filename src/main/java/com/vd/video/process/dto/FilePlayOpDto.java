package com.vd.video.process.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vd.video.process.dto.group.FileAddOrRemove;
import com.vd.video.process.dto.group.FileOperate;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 播放列表操作类实体
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class FilePlayOpDto {

    @ApiModelProperty("屏幕ID")
    @NotBlank(message = "屏幕ID不能为空")
    private String screenId;

    @ApiModelProperty("文件Id")
    @NotNull(message = "文件Id", groups = FileAddOrRemove.class)
    @Min(value = 1, message = "文件Id必须大于1", groups = FileAddOrRemove.class)
    private Long fileId;

    @ApiModelProperty("文件类型：1.视频，2.图片，3.PPT")
    @NotNull(message = "文件类型不为空")
    @Range(min = 1, max = 3, message = "文件类型不合规")
    private Integer fileType;

    @ApiModelProperty("添加或移除(1.添加，2.移除)")
    @NotNull(message = "添加或移除操作参数不可为空", groups = FileAddOrRemove.class)
    @Range(min = 1, max = 2, message = "文件操作类型不合规", groups = FileAddOrRemove.class)
    private Integer addOrRemove;

    @ApiModelProperty("播放操作类型(1.播放，2.暂停，3.上一曲，4.下一曲)")
    @NotNull(message = "操作类型不为空", groups = FileOperate.class)
    @Range(min = 1, max = 4, message = "播放操作类型不合规", groups = FileOperate.class)
    private Integer operateType;
}
