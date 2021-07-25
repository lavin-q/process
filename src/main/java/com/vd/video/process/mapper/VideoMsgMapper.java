package com.vd.video.process.mapper;


import com.vd.video.process.entity.VideoMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoMsgMapper {
    int deleteByPrimaryKey(Long videoId);

    int insert(VideoMsg record);

    int insertSelective(VideoMsg record);

    VideoMsg selectByPrimaryKey(Long videoId);

    int updateByPrimaryKeySelective(VideoMsg record);

    int updateByPrimaryKey(VideoMsg record);

    List<VideoMsg> selectAll();

    int deleteByFileId(@Param("fileId") Long fileId);

    List<VideoMsg> getByFileId(@Param("fileId") Long fileId);
}