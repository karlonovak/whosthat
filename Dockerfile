FROM openjdk:11.0.2-jre-slim-stretch

WORKDIR /opt
VOLUME /tmp

ARG JAR_FILE
ADD ${JAR_FILE} whosthat.jar

RUN mkdir -p work

RUN pwd

RUN apt-get update && apt-get install -y git
RUN apt-get update && apt-get install -y wget

RUN git clone https://github.com/pjreddie/darknet.git
RUN cd darknet && wget https://pjreddie.com/media/files/yolov3.weights

ENTRYPOINT java $JAVA_OPTS \
    -Djava.net.preferIPv4Stack=true \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=9000 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.rmi.port=9000 \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Djava.security.egd=file:/dev/./urandom \
    -jar whosthat.jar
