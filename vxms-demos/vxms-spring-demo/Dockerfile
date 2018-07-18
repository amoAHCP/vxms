# First stage: Copy the project and run mvn clean install in Docker to build the project against Java 9
FROM maven:3.5.4-jdk-11 AS mavenbuilder
MAINTAINER Andy Moncsek <amo.ahcp@gmail.com>
RUN apt-get update && apt-get install -y --no-install-recommends git && rm -rf /var/lib/apt/lists/*
WORKDIR /app
RUN git clone https://github.com/amoAHCP/vxms.git && \
    cd vxms && \
    git checkout java11 && \
    cd vxms-demos/vxms-spring-demo && \
    mvn clean install


# Second stage: Copies the builded application and creates a custom JRE
FROM alpine:3.8 AS builder
MAINTAINER Andy Moncsek <amo.ahcp@gmail.com>
ENV JAVA_HOME=/opt/jdk \
    PATH=${PATH}:/opt/jdk/bin \
    LANG=C.UTF-8
RUN set -ex && \
    apk add --no-cache bash && \
    wget https://download.java.net/java/early_access/alpine/22/binaries/openjdk-11-ea+22_linux-x64-musl_bin.tar.gz -O jdk.tar.gz && \
    mkdir -p /opt/jdk && \
    tar zxvf jdk.tar.gz -C /opt/jdk --strip-components=1 && \
    rm jdk.tar.gz && \
    rm /opt/jdk/lib/src.zip
WORKDIR /app
COPY --from=mavenbuilder /app/vxms/vxms-demos/vxms-spring-demo/ ./
RUN jlink --module-path target/vxms-spring-demo-1.2-SNAPSHOT.jar:target/modules:$JAVA_HOME/jmods \
          --add-modules vxms.spring.demo \
          --limit-modules vxms.spring.demo \
          --launcher run=vxms.spring.demo/org.jacpfx.vxms.spring.SimpleSpringRESTStaticInit \
          --output dist \
          --compress 2 \
          --strip-debug \
          --no-header-files \
          --no-man-pages \
          --vm server


# Third stage: Copies the custom JRE into our image and runs it
FROM alpine:3.8
MAINTAINER Andy Moncsek <amo.ahcp@gmail.com>
WORKDIR /app
COPY --from=builder /app/dist/ ./
EXPOSE 9090
ENV JVM_OPTS="-XX:MaxRAMFraction=1"
#ENV JVM_OPTS="-Xms16M"
CMD ["bin/java", "--add-opens","java.base/java.lang=spring.core","-m","vxms.spring.demo/org.jacpfx.vxms.spring.SimpleSpringRESTStaticInit"]
# CMD ["bin/java", "-m","vxms.spring.demo/org.jacpfx.vxms.spring.SimpleSpringREST", "--illigal-access=deny","--add-opens ","java.base/java.lang=ALL"]
#CMD ["bin/java -m vxms.spring.demo/org.jacpfx.vxms.spring.SimpleSpringREST --illigal-access=deny --add-opens java.base/java.lang=ALL"]



