FROM openjdk:8
MAINTAINER 915940173@qq.com

LABEL name="process" version="1.0" author="qhm"
#tomcat默认文件夹
VOLUME /tmp
#需要和application中server.port保持一致
EXPOSE 8088
#当前包名（process-0.0.1-SNAPSHOT.jar）-转换后的包名（process.jar）
ADD process-0.0.1-SNAPSHOT.jar process.jar
#和转换后的包名一致
RUN bash -c 'touch /process.jar'
#运行时环境变量
ENTRYPOINT ["java","-Xmn512m","-Xms1024m","-Xmx1024m","-XX:MetaspaceSize=1024m","-XX:MaxMetaspaceSize=1024m","-XX:+PrintGCDetails","-XX:+PrintGCTimeStamps","-XX:+PrintHeapAtGC","-Dspring.profiles.active=prod","-jar","process.jar"]
