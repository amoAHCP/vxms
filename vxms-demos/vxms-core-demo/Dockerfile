# First stage: Copy the project and run mvn clean install in Docker to build the project against Java 9
FROM maven:3.5.3-jdk-9 AS mavenbuilder
MAINTAINER Andy Moncsek <amo.ahcp@gmail.com>
RUN apt-get update && apt-get install -y --no-install-recommends git && rm -rf /var/lib/apt/lists/*
WORKDIR /app
RUN git clone https://github.com/amoAHCP/vxms.git && \
    cd vxms && \
    git checkout java9 && \
    cd vxms-demos/vxms-core-demo && \
    mvn clean install


# Second stage: Copies the builded application and creates a custom JRE
FROM alpine:3.7 AS builder
MAINTAINER Andy Moncsek <amo.ahcp@gmail.com>
ENV JAVA_HOME=/opt/jdk \
    PATH=${PATH}:/opt/jdk/bin \
    LANG=C.UTF-8
RUN set -ex && \
    apk add --no-cache bash && \
    wget https://download.java.net/java/early_access/alpine/11/binaries/openjdk-11-ea+11_linux-x64-musl_bin.tar.gz -O jdk.tar.gz && \
    mkdir -p /opt/jdk && \
    tar zxvf jdk.tar.gz -C /opt/jdk --strip-components=1 && \
    rm jdk.tar.gz && \
    rm /opt/jdk/lib/src.zip
WORKDIR /app
COPY --from=mavenbuilder /app/vxms/vxms-demos/vxms-core-demo/ ./
COPY --from=mavenbuilder /app/vxms/vxms-demos/vxms-core-demo/target/mod/vxms-core-1.2-SNAPSHOT.jar ./target/modules
RUN jlink --module-path target/vxms-core-demo-1.2-SNAPSHOT.jar:target/modules:$JAVA_HOME/jmods \
          --add-modules vxms.core.demo \
          --limit-modules vxms.core.demo \
          --launcher run=vxms.core.demo/org.jacpfx.vxms.verticle.SimpleREST \
          --output dist \
          --compress 2 \
          --strip-debug \
          --no-header-files \
          --no-man-pages \
          --vm server


# Third stage: Copies the custom JRE into our image and runs it
FROM alpine:3.7
MAINTAINER Andy Moncsek <amo.ahcp@gmail.com>
WORKDIR /app
COPY --from=builder /app/dist/ ./
EXPOSE 9090
ENV JVM_OPTS="-XX:MaxRAMFraction=1"
#ENV JVM_OPTS="-Xms16M"
ENTRYPOINT ["bin/run"]



