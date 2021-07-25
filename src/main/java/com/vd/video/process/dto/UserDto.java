package com.vd.video.process.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vd.video.process.dto.group.GroupUserChangePassword;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户信息
 */
@Data
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class UserDto implements Serializable {

    @ApiModelProperty("用户名")
    @NotBlank(message = "用户名不可为空")
    private String username;

    @ApiModelProperty("密码")
    @NotBlank(message = "登录密码不可为空")
    private String password;

    @ApiModelProperty("新密码")
    @NotBlank(groups = {GroupUserChangePassword.class},message = "新密码不可为空")
    private String newPassword;

    @ApiModelProperty("确认密码")
    @NotBlank(groups = {GroupUserChangePassword.class},message = "确认密码不可为空")
    private String checkPassword;
}
