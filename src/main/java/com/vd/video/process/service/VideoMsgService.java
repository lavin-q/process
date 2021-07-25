package com.vd.video.process.service;

import com.vd.video.process.entity.FileMsg;
import com.vd.video.process.entity.VideoMsg;
import com.vd.video.process.mapper.FileMsgMapper;
import com.vd.video.process.mapper.VideoMsgMapper;
import com.vd.video.process.utils.FileUtil;
import com.vd.video.process.vo.FileVideoVo;
import com.vd.video.process.vo.FileVo;
import com.vd.video.process.vo.VideoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视频信息Service
 */
@Slf4j
@Component
@Transactional
public class VideoMsgService {
    @Resource
    private VideoMsgMapper videoMsgMapper;
    @Resource
    private FileMsgMapper fileMsgMapper;
    @Value("${file.prefix}")
    private String filePrefix;

    /**
     * 根据文件类型获取文件列表
     *
     * @return 文件列表
     */
    public List<FileVideoVo> getFileList(Integer fileType) {
        List<FileVo> fileVos = this.fileMsgMapper.selectByFileType(fileType);
        return groupByFileName(fileVos);
    }

    /**
     * 根据文件名模糊匹配获取文件信息
     *
     * @param name 文件名
     * @return 文件列表
     */
    public List<FileVideoVo> getFileByName(String name) {
        List<FileVo> fileVos = this.fileMsgMapper.getFileByName(name);
        return groupByFileName(fileVos);
    }

    private List<FileVideoVo> groupByFileName(List<FileVo> fileVos) {
        Map<String, List<FileVo>> collect = fileVos.stream().collect(Collectors.groupingBy(FileVo::getFileName));
        Set<Map.Entry<String, List<FileVo>>> entries = collect.entrySet();
        List<FileVideoVo> result = new ArrayList<>();
        for (Map.Entry<String, List<FileVo>> entry : entries) {
            //文件名
            FileVideoVo fileVideoVo = new FileVideoVo();
            fileVideoVo.setFileName(entry.getKey());
            List<VideoVo> videoVos = new ArrayList<>();
            //遍历
            for (FileVo fileVo : entry.getValue()) {
                fileVideoVo.setFileId(fileVo.getFileId());
                if (!StringUtils.isBlank(fileVo.getFileCoverPath())) {
                    String[] split = fileVo.getFileCoverPath().split("/");
                    fileVideoVo.setFileCoverPath(filePrefix + "/" + split[split.length - 1]);
                }
                fileVideoVo.setFileType(fileVo.getFileType());
                VideoVo videoVo = new VideoVo();
                videoVo.setVideoId(fileVo.getVideoId());
                videoVo.setVideoName(fileVo.getVideoName());
                videoVo.setVideoPath(filePrefix + "/" + fileVo.getVideoName());
                videoVos.add(videoVo);
            }
            fileVideoVo.setFileList(videoVos);
            result.add(fileVideoVo);
        }
        return result;
    }

    /**
     * 根据视频Id获取视频信息
     *
     * @param id 视频ID
     * @return 视频详情
     */
    public VideoMsg getVideoById(Long id) {
        return this.videoMsgMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据文件ID获取视文件息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    public Map<String, Object> getFileById(Long fileId) {
        List<FileVo> files = this.fileMsgMapper.getFileById(fileId);
        if (files == null || files.isEmpty()) {
            return null;
        } else {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("fileName", files.get(0).getFileName());
            resultMap.put("fileId", files.get(0).getFileId());
            if (!StringUtils.isBlank(files.get(0).getFileCoverPath())) {
                String[] split = files.get(0).getFileCoverPath().split("/");
                resultMap.put("fileCoverPath", filePrefix + "/" + split[split.length - 1]);
            }
            resultMap.put("fileType", files.get(0).getFileType());
            List<Map<String, Object>> list = new ArrayList<>();
            for (FileVo file : files) {
                Map<String, Object> map = new HashMap<>();
                map.put("videoPath", filePrefix + "/" + file.getVideoName());
                map.put("videoName", file.getVideoName());
                map.put("videoId", file.getVideoId());
                list.add(map);
            }
            resultMap.put("fileList", list);
            return resultMap;
        }
    }


    /**
     * 新增视频记录
     *
     * @param videoMsg 视频信息
     */
    public void insert(VideoMsg videoMsg) {
        this.videoMsgMapper.insertSelective(videoMsg);
    }

    /**
     * 新增文件记录
     *
     * @param fileMsg 文件信息
     */
    public void insertFile(FileMsg fileMsg) {
        this.fileMsgMapper.insertSelective(fileMsg);
    }

    /**
     * 根据名称查询文件信息
     *
     * @param fileGroupName 文件名
     * @return 文件记录
     */
    public List<FileMsg> selectFileByName(String fileGroupName) {
        List<FileMsg> fileMsgs = this.fileMsgMapper.selectByName(fileGroupName);
        if (Objects.isNull(fileMsgs) || fileMsgs.isEmpty()) {
            return new ArrayList<>();
        } else {

            return fileMsgs;
        }
    }

    /**
     * 根据fileId获取文件记录
     *
     * @param fileId fileId
     * @return fileMsg
     */
    public FileMsg getFileMsgById(Long fileId) {
        FileMsg fileMsg = this.fileMsgMapper.selectByPrimaryKey(fileId);
        if (fileMsg == null) {
            return null;
        } else {
            return fileMsg;
        }
    }

    public void deleteFileAndVideoById(Long fileId) {
        FileMsg fileMsg = this.fileMsgMapper.selectByPrimaryKey(fileId);
        if (Objects.nonNull(fileMsg) && StringUtils.isNotBlank(fileMsg.getFileCoverPath())) {
            boolean b = FileUtil.deleteFile(fileMsg.getFileCoverPath());
            log.info("===========删除文件：{}：{}===============", fileMsg.getFileCoverPath(), (b ? "成功" : "失败"));
        }
        this.fileMsgMapper.deleteByPrimaryKey(fileId);
        List<VideoMsg> byFileId = this.videoMsgMapper.getByFileId(fileId);
        for (VideoMsg videoMsg : byFileId) {
            String videoPath = videoMsg.getVideoPath();
            boolean b = FileUtil.deleteFile(videoPath);
            log.info("===========删除文件：{}：{}===============", videoPath, (b ? "成功" : "失败"));
        }
        this.videoMsgMapper.deleteByFileId(fileId);
    }
}
