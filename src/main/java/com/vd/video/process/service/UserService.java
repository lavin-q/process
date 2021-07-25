package com.vd.video.process.service;

import com.vd.video.process.entity.UserMsg;
import com.vd.video.process.mapper.UserMsgMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Author: qihumao
 * @Description:
 * @Date: 2021-07-02 14:47
 * @Version: 1.0.0
 */
@Component
@Transactional
public class UserService {

    @Resource
    private UserMsgMapper userMsgMapper;

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    public UserMsg getUserMsgByName(String username) {
        return this.userMsgMapper.getUserMsgByName(username);
    }

    /**
     * 新增用户
     *
     * @param userMsg 用户信息
     */
    public void updateUser(UserMsg userMsg) {
        this.userMsgMapper.updateByPrimaryKeySelective(userMsg);
    }
}
