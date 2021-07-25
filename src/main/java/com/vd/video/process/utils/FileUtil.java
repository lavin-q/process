package com.vd.video.process.utils;


import com.vd.video.process.common.ContentTypeUtil;
import com.vd.video.process.constant.PositionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

@Slf4j
public class FileUtil {

    /**
     * 判断文件中是否包含特殊的内容
     *
     * @param filePath 文件路径
     * @param value    内容
     * @return
     */
    public static Boolean contain(String filePath, String value) {
        Boolean result = false;
        BufferedReader br = null;
        String line;
        //保存修改过后的所有内容，不断增加
        StringBuilder bufAll = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                StringBuffer buf = new StringBuffer();
                if (line.contains(value)) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 在文件中查找到KEY，则在KEY前或者后插入VALUE
     *
     * @param filePath
     * @param key
     * @param valueList 需要添加字符列表
     * @return
     */
    public static String insertValueWithKey(String filePath, String key, List<String> valueList, PositionType index) {
        BufferedReader br = null;
        String line;
        //保存修改过后的所有内容，不断增加
        StringBuilder bufAll = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                StringBuffer buf = new StringBuffer();

                Pattern p = compile(key);
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    if (index == PositionType.AFTER) {
                        buf.append(line + "\n");
                    }

                    for (String value : valueList) {
                        buf.append(value + "\n");
                    }
                    if (index == PositionType.BEFORE) {
                        buf.append(line + "\n");
                    }

                    bufAll.append(buf);
                } else {
                    bufAll.append(line + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bufAll.toString();
    }

    /**
     * 在文件中查找多个KEY，则在KEY的前或者后插入VALUE
     *
     * @param filePath
     * @param keys
     * @param valueList 需要添加字符列表
     * @return
     */
    public static String insertValueWithKeys(String filePath, List<String> keys, List<String> valueList, PositionType index) {
        BufferedReader br = null;
        String line;
        StringBuilder lineBuf = new StringBuilder();
        Integer matchIndex = 0;
        //保存修改过后的所有内容，不断增加
        StringBuilder bufAll = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(filePath));

            while ((line = br.readLine()) != null) {
                StringBuffer buf = new StringBuffer();


                Pattern p = compile(keys.get(matchIndex));
                Matcher m = p.matcher(line);
                lineBuf.append(line + "\n");
                if (m.matches()) {
                    matchIndex++;
                    if (matchIndex < keys.size()) {
                        continue;
                    }
                }

                //所有都匹配
                if (matchIndex == keys.size()) {
                    if (index == PositionType.AFTER) {
                        buf.append(lineBuf);
                        lineBuf.delete(0, lineBuf.length());
                    }

                    for (String value : valueList) {
                        buf.append(value + "\n");
                    }
                    if (index == PositionType.BEFORE) {
                        buf.append(lineBuf);

                        lineBuf.delete(0, lineBuf.length());
                    }

                    bufAll.append(buf);
                    matchIndex = 0;
                } else {

                    bufAll.append(lineBuf);
                    lineBuf.delete(0, lineBuf.length());
                    matchIndex = 0;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bufAll.toString();
    }


    /**
     * 获取最后一个空格的位置
     *
     * @param line
     * @return
     */
    private static int getlastblankSpaceIndex(String line) {
        int i = 0;
        for (i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') {
                return i;
            }
        }
        return i;
    }

    /**
     * 将内容写入文件
     *
     * @param filePath 文件路径
     * @param content  待写入内容
     */
    public static void writeFile(String filePath, String content) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建文件目录
     *
     * @param path 路径
     */
    public static boolean createDir(String path) {
        boolean flag = true;
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        try {
            Path path1 = Paths.get(path);
            if (!Files.exists(path1)) {
                //创建多级目录
                Files.createDirectories(path1);
            }
        } catch (IOException e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    public static Map<String, String> uploadFile(MultipartFile file, String dirPath) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        String shortPath = file.getOriginalFilename();
        //如果获取文件名失败
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        if (StringUtils.isEmpty(shortPath)) {
            String contentType = file.getContentType();
            String fileContentType = ContentTypeUtil.getByHttpContentType(contentType);
            //String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
            shortPath = uuid + fileContentType;
        } else {
            String substring1 = shortPath.substring(0, shortPath.lastIndexOf('.'));
            substring1 = substring1 + "_" + uuid;
            String substring2 = shortPath.substring(shortPath.lastIndexOf('.'));
            shortPath = substring1 + substring2;
        }
        File dest = new File(dirPath, shortPath);
        if (!dest.getParentFile().exists()) {
            boolean rel = dest.getParentFile().mkdirs();
            if (!rel) {
                throw new Exception("文件夹创建失败");
            }
        }
        resultMap.put("fileName", shortPath);
        InputStream is = file.getInputStream();
        OutputStream os = new FileOutputStream(dest);
        try {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            is.close();
            os.close();
            resultMap.put("filePath", dest.getPath());
        }
        return resultMap;
    }

    public static boolean deleteFile(String filePath) {
        try {
            //判断文件是否存在
            if (StringUtils.isBlank(filePath)) {
                //文件路径为空，删除失败
                log.info("文件路径为空，删除失败！");
                return false;
            }
            File file = new File(filePath);
            if (file.isFile()) {
                file.deleteOnExit();
                log.info("删除成功");
                return true;
            } else {
                //不是一个文件
                log.info("所传路径不是一个文件路径，删除失败！");
                return false;
            }
        } catch (Exception e) {
            log.error("删除文件出错：{}", e.getMessage());

        }
        return false;
    }
}
