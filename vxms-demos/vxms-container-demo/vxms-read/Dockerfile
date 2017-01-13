FROM maven:3.3-jdk-8-onbuild
COPY . /usr/src/app


CMD ["java", "-jar", "/usr/src/app/target/vxms-read-1.0-SNAPSHOT-fat.jar","-instances","8"]
