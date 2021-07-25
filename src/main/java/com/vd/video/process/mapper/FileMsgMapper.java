package com.vd.video.process.mapper;


import com.vd.video.process.entity.FileMsg;
import com.vd.video.process.vo.FileVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMsgMapper {
    int deleteByPrimaryKey(Long fileId);

    int insert(FileMsg record);

    int insertSelective(FileMsg record);

    FileMsg selectByPrimaryKey(Long fileId);

    int updateByPrimaryKeySelective(FileMsg record);

    int updateByPrimaryKey(FileMsg record);

    List<FileMsg> selectByName(@Param("name") String name);

    List<FileVo> getFileByName(@Param("name") String name);

    List<FileVo> getFileById(@Param("fileId") Long fileId);

    List<FileVo> selectAll();

    List<FileVo> selectByFileType(@Param("fileType") Integer fileType);
}