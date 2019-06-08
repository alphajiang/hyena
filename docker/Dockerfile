FROM ubuntu:latest

ENV APP_PATH /opt/hyena
WORKDIR $APP_PATH

RUN mv /etc/apt/sources.list /etc/apt/sources.list.bak
RUN sed 's/archive.ubuntu.com/mirrors.aliyun.com/' /etc/apt/sources.list.bak > /etc/apt/sources.list
RUN apt update -y
RUN apt upgrade -y


ENV TZ 'Asia/Shanghai'
RUN echo $TZ > /etc/timezone && \
    apt install -y tzdata && \
    rm /etc/localtime && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    dpkg-reconfigure -f noninteractive tzdata && \
    apt clean

RUN apt install -y openjdk-11-jre-headless redis-server


EXPOSE 8080

ADD start.sh $APP_PATH/
ADD hyena.jar $APP_PATH/



CMD ./start.sh


