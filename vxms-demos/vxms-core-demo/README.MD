# a basic vxms-core example

This example shows the minimum usage of vxms-core. It defines a HTTP endpoint on port 9090 and two REST (Get) endpoints. 
You can start the service using the *main* method or you build the project (*mvn package*) and run the fat jar (*java -jar target/vxms-core-demo-one-fat.jar*).

## run as Java 9 module
In this case we simply use the main method declared in the class, instead of using the Vert.x Factory mechanism used by maven shade.

```shell
   java --module-path target/vxms-core-demo-1.2-SNAPSHOT.jar:target/mod --module vxms.core.demo/org.jacpfx.vxms.verticle.SimpleREST
  
``` 

## create a modular runtime image
The Dockerfile in this project is an example, how to build a modular runtime image to run a vxms (Vert.x based) application. To build the application modules, the moditec maven plugin is used(https://github.com/moditect/moditect). Basically the process consists of 3 Steps:
- Step 1: clone the project, switch to Java9 branch and build the project against Java9
- Step 2: the created modules are copied to the next step, the alpine JDK11 will be downloaded an a modular runtime image will be created
- Step 3: the modular image (containing the REST service) will be copied to an alpine image

### how to build
```shell
docker build -t core-demo .
```
### how to run
```shell
docker run -m32M -p 9090:9090 --name core-demo core-demo:latest
```
