package com.vd.video.process.controller;

import com.vd.video.process.common.Result;
import com.vd.video.process.dto.UserDto;
import com.vd.video.process.dto.group.GroupUserChangePassword;
import com.vd.video.process.entity.UserMsg;
import com.vd.video.process.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Api("用户管理api")
public class UserController {
    private static Map<String, Boolean> loginMap = new HashMap<>();

    @Resource
    private UserService userService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Object login(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (ObjectError error : bindingResult.getAllErrors()) {
                sb.append(error.getDefaultMessage()).append(",");
            }
            return Result.error(sb.toString());
        }
        String username = userDto.getUsername();
        String password = userDto.getPassword();
        UserMsg userMsgByName = this.userService.getUserMsgByName(username);
        if (userMsgByName == null) {
            return Result.error("用户名或密码错误");
        }
        if (username.equals(userMsgByName.getUsername()) && password.equals(userMsgByName.getPassword())) {
            loginMap.put(username, true);
            return Result.ok("登录成功");
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Object logout(@RequestParam String username) {
        if (StringUtils.isBlank(username)) {
            Result.error("用户名不可为空");
        }
        Boolean aBoolean = loginMap.get(username);
        if (aBoolean != null) {
            loginMap.remove(username);
        }
        return Result.ok("退出成功");
    }

    @ApiOperation("修改密码")
    @PostMapping("/change")
    public Object change(@Validated(value = {GroupUserChangePassword.class, Default.class}) @RequestBody UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (ObjectError error : bindingResult.getAllErrors()) {
                sb.append(error.getDefaultMessage()).append(",");
            }
            return Result.error(sb.toString());
        }
        if (userDto == null
                || StringUtils.isBlank(userDto.getUsername())
                || StringUtils.isBlank(userDto.getPassword())
                || StringUtils.isBlank(userDto.getCheckPassword())
                || StringUtils.isBlank(userDto.getNewPassword())) {
            return Result.error("参数异常");
        }
        String username = userDto.getUsername();
        String password = userDto.getPassword();
        String checkPassword = userDto.getCheckPassword();
        String newPassword = userDto.getNewPassword();
        if (!newPassword.equals(checkPassword)) {
            return Result.error("两次密码不匹配！");
        }
        UserMsg userMsgByName = this.userService.getUserMsgByName(username);
        if (null == userMsgByName) {
            return Result.error("用户名或密码错误");
        }
        if (username.equals(userMsgByName.getUsername()) && password.equals(userMsgByName.getPassword())) {
            userMsgByName.setPassword(checkPassword);
            userMsgByName.setUpdateTime(new Date());
            this.userService.updateUser(userMsgByName);
        } else {
            return Result.error("用户名或密码错误");
        }
        return Result.ok("修改成功");
    }

}
