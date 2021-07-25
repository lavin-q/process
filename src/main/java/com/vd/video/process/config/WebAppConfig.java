package com.vd.video.process.config;


import com.vd.video.process.controller.VideoController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter {

    @Value("${video.resource}")
    private String videoResource;
    @Value("${file.prefix}")
    private String prefix = "/file";
    @Value("${file.pattern}")
    private String pattern = "/file/**";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                .maxAge(10000);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //文件最大KB,MB
        factory.setMaxFileSize(DataSize.parse("5120MB"));
        //设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.parse("5120MB"));
        return factory.createMultipartConfig();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (videoResource.equals("") || videoResource.equals("${video.resource}")) {
            String imagesPath = WebAppConfig.class.getClassLoader().getResource("").getPath();
            System.out.print("1.上传配置类imagesPath==" + imagesPath + "\n");
            if (imagesPath.indexOf(".jar") > 0) {
                imagesPath = imagesPath.substring(0, imagesPath.indexOf(".jar"));
            } else if (imagesPath.indexOf("classes") > 0) {
                imagesPath = "file:" + imagesPath.substring(0, imagesPath.indexOf("classes"));
            }
            imagesPath = imagesPath.substring(0, imagesPath.lastIndexOf("/")) + prefix + "/";
            videoResource = imagesPath;
        }
        System.out.print("imagesPath=============" + videoResource + "\n");
        //LoggerFactory.getLogger(WebAppConfig.class).info("imagesPath============="+mImagesPath+"\n");
        registry.addResourceHandler(pattern).addResourceLocations(videoResource);
        // TODO Auto-generated method stub
        System.out.print("2.上传配置类mImagesPath==" + videoResource + "\n");
        super.addResourceHandlers(registry);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //配置生成器：添加一个拦截器，拦截路径为API以后的路径
        registry.addInterceptor(requestInterceptor())
                .addPathPatterns("/**");
        //super.addInterceptors(registry);
    }
}

class RequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String, Integer> dataMap = VideoController.getDataMap();
        dataMap.put("requestCount", dataMap.get("requestCount") == null ? 1 : dataMap.get("requestCount") + 1);
        VideoController.setDataMap(dataMap);
        return true;
    }
}
