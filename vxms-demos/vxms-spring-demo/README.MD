# A  vxms REST + Spring endpoint 
This example demonstrates the usage of the spring-vertx-ext together with vxms. 
Start the Endpoint either by using the main method or build the project und start the jar file. 



## run as Java 9 module
In this case we simply use the main method declared in the class, instead of using the Vert.x Factory mechanism used by maven shade.

```shell
   java --module-path target/vxms-spring-demo-1.2-SNAPSHOT.jar:target/modules:$JAVA_HOME/jmods/ --module vxms.spring.demo/org.jacpfx.vxms.spring.SimpleSpringREST
  
``` 

## create a modular runtime image
The Dockerfile in this project is an example, how to build a modular runtime image to run a vxms (Vert.x based) application. To build the application modules, the moditec maven plugin is used(https://github.com/moditect/moditect). Basically the process consists of 3 Steps:
- Step 1: clone the project, switch to Java9 branch and build the project against Java9
- Step 2: the created modules are copied to the next step, the alpine JDK11 will be downloaded an a modular runtime image will be created
- Step 3: the modular image (containing the REST service) will be copied to an alpine image

### how to build
```shell
docker build -t spring-demo .
```
### how to run
```shell
docker run -m32M -p 9090:9090 --name spring-demo spring-demo:latest
```

