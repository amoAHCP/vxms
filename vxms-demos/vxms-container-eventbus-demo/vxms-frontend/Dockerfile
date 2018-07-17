FROM maven:3.3-jdk-8
COPY . /usr/src/app

ENV httpPort 8181
EXPOSE $httpPort
# CMD ["java", "-jar", "/usr/src/app/target/vxms-frontend-1.0-SNAPSHOT-fat.jar","-instances","8","-d64","-server","-XX:+AggressiveOpts"]
CMD ["java", "-jar", "/usr/src/app/target/vxms-frontend-1.2-SNAPSHOT-fat.jar","-cluster"]
