FROM anapsix/alpine-java:8_jdk_unlimited

MAINTAINER Maskym Ostroverkhov <m.ostroverkhov@gmail.com>

WORKDIR /opt/firebase-server

ADD target/firebase-rsocket-server-binary-tcp* /opt/firebase-server/fserver.jar

ENTRYPOINT ["java", "-jar", "fserver.jar","--port","8090"]

EXPOSE 8090

