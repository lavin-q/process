package com.vd.video.process.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vd.video.process.common.Result;
import com.vd.video.process.dto.FileDto;
import com.vd.video.process.dto.FilePlayOpDto;
import com.vd.video.process.dto.VideoDto;
import com.vd.video.process.dto.group.FileOperate;
import com.vd.video.process.entity.FileMsg;
import com.vd.video.process.entity.VideoMsg;
import com.vd.video.process.service.VideoMsgService;
import com.vd.video.process.utils.FileUtil;
import com.vd.video.process.vo.FileVideoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.*;

/**
 * 文件管理Api
 */
@Slf4j
@RestController
@RequestMapping("/video")
@Api("视频管理api")
public class VideoController {
    //播放列表Map<大屏标志(abcd),Map<文件类型(1:视频，2：图片，3：ppt),List<FileDto>>>
    private static LinkedHashMap<String, LinkedHashMap<Integer, List<FileVideoVo>>> playerListMap = new LinkedHashMap<>();

    //文件-播放列表 Map<文件ID,大屏名称/标志>
    private static Map<Long, String> filePlayerMap = new HashMap<>();

    //数据
    private static Map<String, Integer> dataMap = new HashMap() {{
        put("fileUpdateCount", 0);
        put("playListUpdateCount", 0);
        put("requestCount", 0);
    }};

    public static Map<String, Integer> getDataMap() {
        return dataMap;
    }

    public static void setDataMap(Map<String, Integer> dataMap) {
        VideoController.dataMap = dataMap;
    }

    @Value("${video.path}")
    private String videoPath;

    @Resource
    private VideoMsgService videoMsgService;


    @ApiOperation("根据文件类型获取文件列表")
    @GetMapping("/findFileList/{fileType}")
    public Object findList(@PathVariable Integer fileType) {
        if (fileType == null) {
            return Result.error("参数异常");
        }
        List<FileVideoVo> fileVideoList = this.videoMsgService.getFileList(fileType);
        return Result.ok(fileVideoList);
    }

    @ApiOperation("根据ID查询文件")
    @GetMapping("/findFile/{id}")
    public Object findVideo(@PathVariable Long id) {
        if (Objects.isNull(id) || id < 0) {
            return Result.error("参数异常");
        }
        Map<String, Object> fileById = this.videoMsgService.getFileById(id);
        if (Objects.isNull(fileById)) {
            return Result.ok();
        } else {
            return Result.ok(fileById);
        }
    }

    @ApiOperation("根据名称模糊查询文件列表")
    @GetMapping("/findFileByName/{name}")
    public Object findVideoByName(@PathVariable String name) {
        List<FileVideoVo> fileByName = this.videoMsgService.getFileByName(name);
        if (Objects.isNull(fileByName)) {
            return Result.ok();
        } else {
            return Result.ok(fileByName);
        }
    }

    @ApiOperation("文件上传")
    @PostMapping("/uploadFile")
    public Object uploadVideo(@RequestParam(required = false) MultipartFile file) {
        try {
            long start = System.currentTimeMillis();
            log.info("upload start = " + start);
            Map<String, String> fileMap = FileUtil.uploadFile(file, videoPath);
            long end = System.currentTimeMillis();
            log.info("upload end = " + end);
            log.info("上传总耗时:{}ms", end - start);
            return Result.ok(fileMap);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    @ApiOperation("视频信息入库")
    @PostMapping("/insertFileRecord")
    public Object insertVideoRecord(@Valid @RequestBody FileDto fileDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (ObjectError error : bindingResult.getAllErrors()) {
                sb.append(error.getDefaultMessage()).append(",");
            }
            return Result.error(sb.toString());
        }
        if (fileDto.getFileType() == 1 && StringUtils.isBlank(fileDto.getFileCoverPath())) {
            return Result.error("视频封面路径不可为空！");
        }
        List<FileMsg> fileMsgs = this.videoMsgService.selectFileByName(fileDto.getFileGroupName());
        if (fileMsgs != null && fileMsgs.size() > 0) {
            return Result.error("文件名已存在！");
        } else {
            //插入file记录
            FileMsg fileMsg = new FileMsg();
            fileMsg.setFileGroupName(fileDto.getFileGroupName());
            fileMsg.setFileType(fileDto.getFileType());
            if (!StringUtils.isBlank(fileDto.getFileCoverPath())) {
                fileMsg.setFileCoverPath(fileDto.getFileCoverPath());
            }
            this.videoMsgService.insertFile(fileMsg);
            Long fileId = fileMsg.getFileId();
            //插入video记录
            List<VideoDto> videoList = fileDto.getVideoList();
            for (VideoDto videoDto : videoList) {
                VideoMsg videoMsg = new VideoMsg();
                videoMsg.setVideoName(videoDto.getFileName());
                videoMsg.setVideoPath(videoDto.getFilePath());
                videoMsg.setFileId(fileId);
                this.videoMsgService.insert(videoMsg);
            }
        }
        dataMap.put("fileUpdateCount", dataMap.get("fileUpdateCount") == null ? 1 : dataMap.get("fileUpdateCount") + 1);
        return Result.ok();
    }

    @ApiOperation("视频删除")
    @PostMapping("/deleteFile")
    public Object deleteVideo(@ApiParam("文件ID") @RequestParam(name = "fileId") Long fileId) {
        //删除file记录，并附带删除video记录
        if (Objects.isNull(fileId) || fileId < 0) {
            return Result.error("参数异常");
        }

        FileMsg fileMsgById = this.videoMsgService.getFileMsgById(fileId);
        if (fileMsgById != null) {
            boolean flag = true;
            //遍历播放列表
            Integer fileType = fileMsgById.getFileType();
            Set<Map.Entry<String, LinkedHashMap<Integer, List<FileVideoVo>>>> entries = playerListMap.entrySet();
            for (Map.Entry<String, LinkedHashMap<Integer, List<FileVideoVo>>> entry : entries) {
                LinkedHashMap<Integer, List<FileVideoVo>> value = entry.getValue();
                List<FileVideoVo> fileVideoVos = value.get(fileType);
                if (fileVideoVos != null) {
                    Optional<FileVideoVo> any = fileVideoVos.stream().filter(item -> item.getFileId().equals(fileId)).findAny();
                    if (any.isPresent()) {
                        flag = false;
                        break;
                    }
                }
            }
            if (!flag) {
                return Result.error("请先移除播放列表后再删除文件！");
            }
            this.videoMsgService.deleteFileAndVideoById(fileId);
        }
        dataMap.put("fileUpdateCount", dataMap.get("fileUpdateCount") == null ? 1 : dataMap.get("fileUpdateCount") + 1);
        return Result.ok("删除成功");
    }


    //获取播放列表，需要携带当前播放的视频信息，如不携带，默认无播放
    @ApiOperation("获取屏幕播放列表(根据屏幕ID)")
    @GetMapping("/getPlayList")
    public Object getPlayList(@ApiParam("屏幕ID") @RequestParam("screenId") String screenId,
                              @ApiParam("正在播放的文件ID") @RequestParam(required = false, name = "fileId") Long fileId,
                              @ApiParam("正在播放的文件类型") @RequestParam(required = false, name = "fileType") Integer fileType,
                              @ApiParam("播放状态(1.播放，2.暂停)") @RequestParam(required = false, name = "playStatus") Integer playStatus) {
        if (StringUtils.isBlank(screenId)) {
            return Result.error("参数异常");
        }
        LinkedHashMap<Integer, List<FileVideoVo>> videoTypeMap = playerListMap.get(screenId);
        if (fileId != null && fileType != null) {
            List<FileVideoVo> fileVideoVos = videoTypeMap.get(fileType);
            if (fileVideoVos == null || fileVideoVos.isEmpty()) {
                return Result.error("指定的文件不存在！");
            } else {
                //文件类型存在，将所有文件类型文件播放状态变为0
                Set<Map.Entry<Integer, List<FileVideoVo>>> entries = videoTypeMap.entrySet();
                for (Map.Entry<Integer, List<FileVideoVo>> entry : entries) {
                    List<FileVideoVo> value = entry.getValue();
                    for (FileVideoVo fileVideoVo : value) {
                        fileVideoVo.setPlayStatus(0);
                    }
                }
                //文件类型存在，判断是否存在实体
                Optional<FileVideoVo> any = fileVideoVos.stream().filter(item -> item.getFileId().equals(fileId)).findAny();
                if (any.isPresent()) {
                    //如果存在当前文件id,将该类型其他文件置为未播放状态
                    for (FileVideoVo ignored : fileVideoVos) {
                        ignored.setPlayStatus(0);
                        if (ignored.getFileId().equals(fileId)) {
                            if (playStatus != null && (playStatus == 1 || playStatus == 2)) {
                                ignored.setPlayStatus(playStatus);
                            } else {
                                ignored.setPlayStatus(1);
                            }
                        }
                    }
                } else {
                    log.info("指定文件类型中指定的文件不存在，默认播放指定文件类型的第一个文件！");
                    fileVideoVos.get(0).setPlayStatus(1);
                }
            }
            videoTypeMap.put(fileType, fileVideoVos);
            playerListMap.put(screenId, videoTypeMap);
        }
        return Result.ok(videoTypeMap);
    }


    //播放列表文件--添加/移除
    @ApiOperation("播放列表文件添加/移除(根据屏幕ID)")
    @PostMapping("/addOrRemoveFile")
    public Object addToPlayList(@RequestBody Map<String, Object> playListMap) {
        if (playListMap == null || playListMap.isEmpty() || playListMap.get("screenId") == null
                || playListMap.get("fileType") == null || playListMap.get("fileList") == null) {
            return Result.error("参数异常");
        }
        try {
            String screenId = playListMap.get("screenId").toString();
            Integer fileType = Integer.parseInt(playListMap.get("fileType").toString());

            List<FileVideoVo> fileList = JSONObject.parseArray(JSON.toJSONString(playListMap.get("fileList")), FileVideoVo.class);

            LinkedHashMap<Integer, List<FileVideoVo>> playMap = playerListMap.get(screenId);
            if (playMap == null || playMap.isEmpty()) {
                playMap = new LinkedHashMap<>();
            }
            if (fileList == null || fileList.isEmpty()) {
                fileList = new ArrayList<>();
            } else {
                //播放状态全部置为0
                for (FileVideoVo fileVideoVo : fileList) {
                    fileVideoVo.setPlayStatus(0);
                }
            }
            playMap.put(fileType, fileList);
            playerListMap.put(screenId, playMap);
            return Result.ok(playMap);
        } catch (NumberFormatException e) {
            log.info("数据类型转换异常：{}", e.getMessage());
            return Result.error("服务器异常！");
        }

        /*if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (ObjectError error : bindingResult.getAllErrors()) {
                sb.append(error.getDefaultMessage()).append(",");
            }
            return Result.error(sb.toString());
        }
        LinkedHashMap<Integer, List<FileVideoVo>> playMap = playerListMap.get(filePlayOpDto.getScreenId());
        Integer addOrRemove = filePlayOpDto.getAddOrRemove();
        if (playMap == null || playMap.isEmpty()) {
            playMap = new LinkedHashMap<>();
            List<FileVideoVo> fileVideoVos = new ArrayList<>();
        }

        List<FileVideoVo> fileVideoVos = playMap.get(filePlayOpDto.getFileType());
        if (fileVideoVos == null || fileVideoVos.isEmpty()) {
            fileVideoVos = new ArrayList<>();
        }
        if (addOrRemove == 1) {
            //添加
            Map<String, Object> fileById = this.videoMsgService.getFileById(filePlayOpDto.getFileId());
            if (fileById != null && !fileById.isEmpty() && fileById.get("fileType").equals(filePlayOpDto.getFileType())) {
                FileVideoVo fileVideoVo = JSON.parseObject(JSON.toJSONString(fileById), FileVideoVo.class);
                fileVideoVos.add(fileVideoVo);
                log.info("在屏幕ID为：{}的播放列表中添加类型为：{},文件ID为：{}的文件，添加成功！",
                        filePlayOpDto.getScreenId(), filePlayOpDto.getFileType(), filePlayOpDto.getFileId());
                filePlayerMap.put(filePlayOpDto.getFileId(), filePlayOpDto.getScreenId());
            } else {
                log.info("未找到类型为：{}指定文件，添加失败!");
            }
        } else {
            //移除
            boolean b = fileVideoVos.removeIf(fileVideoVo -> fileVideoVo.getFileId().equals(filePlayOpDto.getFileId()));
            if (b) {
                log.info("在屏幕ID为：{}的播放列表中找到类型为：{},文件ID为：{}的文件，移除成功！",
                        filePlayOpDto.getScreenId(), filePlayOpDto.getFileType(), filePlayOpDto.getFileId());
                filePlayerMap.remove(filePlayOpDto.getFileId());
            } else {
                log.info("在屏幕ID为：{}的播放列表中找到类型为：{},文件ID为：{}的文件，移除失败！",
                        filePlayOpDto.getScreenId(), filePlayOpDto.getFileType(), filePlayOpDto.getFileId());
            }
            //如果移除的是正在播放的文件，则将正在播放设为第一个视频
            if (!fileVideoVos.isEmpty()) {
                Optional<FileVideoVo> any = fileVideoVos.stream().filter(item -> item.getPlayStatus().equals(1) || item.getPlayStatus().equals(2)).findAny();
                if (!any.isPresent()) {
                    fileVideoVos.get(0).setPlayStatus(1);
                }
            }
        }*/
        /*dataMap.put("playListUpdateCount", dataMap.get("playListUpdateCount") == null ? 1 : dataMap.get("playListUpdateCount") + 1);
        playMap.put(filePlayOpDto.getFileType(), fileVideoVos);
        playerListMap.put(filePlayOpDto.getScreenId(), playMap);*/
        //return Result.ok(playMap);

    }

    //上一曲，下一曲，播放，暂停
    @ApiOperation("播放/暂停/上一曲/下一曲(根据屏幕ID)")
    @PostMapping("/controlScreen")
    public Object controlScreen(@Validated(value = {FileOperate.class, Default.class}) @RequestBody FilePlayOpDto filePlayOpDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (ObjectError error : bindingResult.getAllErrors()) {
                sb.append(error.getDefaultMessage()).append(",");
            }
            return Result.error(sb.toString());
        }
        LinkedHashMap<Integer, List<FileVideoVo>> playMap = playerListMap.get(filePlayOpDto.getScreenId());
        if (playMap != null && !playMap.isEmpty()) {
            //遍历列表，所有都置为0
            Set<Map.Entry<Integer, List<FileVideoVo>>> entries = playMap.entrySet();
            for (Map.Entry<Integer, List<FileVideoVo>> entry : entries) {
                List<FileVideoVo> value = entry.getValue();
                for (FileVideoVo fileVideoVo : value) {
                    fileVideoVo.setPlayStatus(0);
                }
            }
            List<FileVideoVo> fileVideoVos = playMap.get(filePlayOpDto.getFileType());
            if (fileVideoVos != null && !fileVideoVos.isEmpty()) {
                Optional<FileVideoVo> any = fileVideoVos.stream().filter(item -> item.getFileId().equals(filePlayOpDto.getFileId())).findAny();
                if (any.isPresent()) {
                    FileVideoVo fileVideoVo = any.get();
                    //当前正在播放的或指定要播放的文件
                    int i = fileVideoVos.indexOf(fileVideoVo);
                    int k = 0;
                    int size = fileVideoVos.size();
                    switch (filePlayOpDto.getOperateType()) {
                        //播放
                        case 1:
                            fileVideoVos.get(i).setPlayStatus(1);
                            break;
                        //暂停
                        case 2:
                            fileVideoVos.get(i).setPlayStatus(2);
                            break;
                        //上一曲
                        case 3:
                            if (i == 0) {
                                k = fileVideoVos.size() - 1;
                            } else {
                                k = i - 1;
                            }
                            fileVideoVos.get(k).setPlayStatus(1);
                            fileVideoVos.get(i).setPlayStatus(0);
                            break;
                        //下一曲
                        case 4:
                            if (i != size - 1) {
                                k = i + 1;
                            }
                            fileVideoVos.get(k).setPlayStatus(1);
                            fileVideoVos.get(i).setPlayStatus(0);
                            break;
                    }
                } else {
                    log.info("未查询到正在播放暂停的文件，默认播放第一个");
                    fileVideoVos.get(0).setPlayStatus(1);
                }
                playMap.put(filePlayOpDto.getFileType(), fileVideoVos);
                playerListMap.put(filePlayOpDto.getScreenId(), playMap);
                return Result.ok(playMap);
            } else {
                return Result.error("当前类型文件播放列表为空，请添加文件后操作");
            }
        } else {
            return Result.error("播放列表为空，请添加文件后操作");
        }

    }

    //操作数据
    @ApiOperation("获取操作文件和播放列表的次数")
    @GetMapping("/getUpdateData")
    public Object getUpdateData() {
        return Result.ok(dataMap);
    }
}
