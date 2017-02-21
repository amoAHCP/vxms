FROM maven:3.3-jdk-8-onbuild
COPY . /usr/src/app

ENV httpPort 8181
EXPOSE $httpPort
# CMD ["java", "-jar", "/usr/src/app/target/vxms-frontend-1.0-SNAPSHOT-fat.jar","-instances","8","-d64","-server","-XX:+AggressiveOpts"]
CMD ["java", "-jar", "/usr/src/app/target/vxms-frontend-1.0-SNAPSHOT-fat.jar","-instances","8","-d64","-XX:+UseG1GC","-XX:MaxGCPauseMillis=500","-XX:+PrintGCTimeStamp"]
