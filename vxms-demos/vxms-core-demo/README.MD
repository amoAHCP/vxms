# a basic vxms-core example

This example shows the minimum usage of vxms-core. It defines a HTTP endpoint on port 9090 and two REST (Get) endpoints. 
You can start the service using the *main* method or you build the project (*mvn package*) and run the fat jar (*java -jar target/vxms-core-demo-one-fat.jar*).

## run as Java 9 module
In this case we simply use the main method declared in the class, instead of using the Vert.x Factory mechanism used by maven shade.

```shell
   java --module-path target/vxms-core-demo-1.2-SNAPSHOT.jar:target/mod --module vxms.core.demo/org.jacpfx.vxms.verticle.SimpleREST
  
``` 


java --module-path target/vxms-core-demo-1.2-SNAPSHOT.jar:target/modules/ --add-modules io.netty.channel.unix,io.netty.channel.kqueue,io.netty.channel.epoll,io.netty.relver.dns,io.netty.codec.dns,io.netty.transport,io.netty.resolver --module vxms.core.demo/org.jacpfx.vxms.verticle.SimpleREST




/Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home/bin/jlink --module-path target/vxms-core-demo-1.2-SNAPSHOT.jar:target/modules:$JAVA_HOME/jmods --add-modules vxms.core.demo --launcher run=vxms.core.demo/org.jacpfx.vxms.verticle.SimpleREST --output java9-vertx --compress 2 --strip-debug --no-header-files --no-man-pages
