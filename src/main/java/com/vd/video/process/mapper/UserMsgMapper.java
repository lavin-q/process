package com.vd.video.process.mapper;


import com.vd.video.process.entity.UserMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMsgMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserMsg record);

    int insertSelective(UserMsg record);

    UserMsg selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UserMsg record);

    int updateByPrimaryKey(UserMsg record);

    UserMsg getUserMsgByName(@Param("username") String name);
}