FROM openjdk:8
LABEL authors="Liuyang"

# 配置
ENV PARAMS=""
# 时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 添加应用
ADD target/chatgpt-0.0.1-SNAPSHOT.jar /chatgpt.jar
# 镜像运行为容器后执行的命令
ENTRYPOINT ["sh", "-c", "java -jar $JAVA_OPTS /chatgpt.jar $PARAMS"]