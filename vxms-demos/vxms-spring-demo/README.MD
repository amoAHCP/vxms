# A  vxms REST + Spring endpoint 
This example demonstrates the usage of the spring-vertx-ext together with vxms. 
Start the Endpoint either by using the main method or build the project und start the jar file. 


TEST

java --module-path target/vxms-spring-demo-1.2-SNAPSHOT.jar:target/mod:/Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home/jmods/ --add-modules java.sql --module vxms.spring.demo/org.jacpfx.vxms.spring.SimpleSprinESTStaticInit

java --module-path target/vxms-spring-demo-1.2-SNAPSHOT.jar:target/mod:/Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home/jmods/ --module vxms.spring.demo/org.jacpfx.vxms.spring.SimpleSpringREST

$JAVA_HOME/bin/jlink --module-path target/vxms-spring-demo-1.2-SNAPSHOT.jar:target/modules:$JAVA_HOME/jmods/ --add-modules vxms.spring.demo --launcher run=vxms.spring.demo/org.jacpfx.vxms.spring.SimpleSpringREST --output=dist --compress=2



jdeps --generate-module-info /Users/amo/Documents/development/vertx/vxms/vxms-demos/vxms-spring-demo/target/moditect --add-modules spring.jcl --module-path /Users/amo/.m2/repository/org/jetbrains/annotations/13.0/annotations-13.0.jar:/Users/amo/.m2/repository/org/aspectj/aspectjweaver/1.8.10/aspectjweaver-1.8.10.jar:/Users/amo/.m2/repository/org/springframework/spring-jcl/5.0.6.RELEASE/spring-jcl-5.0.6.RELEASE.jar /Users/amo/.m2/repository/org/springframework/spring-core/5.0.6.RELEASE/spring-core-5.0.6.RELEASE.jar




io.netty.buffer,io.netty.common,reactor.core,org.reactivestreams,io.reactivex.rxjava2,rxjava,rxjava.reactive.streams,jopt.simple,kotlin.reflect,kotlin.stdlib




jdeps --generate-module-info /Users/amo/Documents/development/vertx/vxms/vxms-demos/vxms-spring-demo/target/moditect -filter io.netty.buffer,io.netty.common,reactor.core,org.reactivestreams,io.reactivex.rxjava2,rxjava,rxjava.reactive.streams,jopt.simple,kotlin.reflect,kotlin.stdlib --add-modules aspectjweaver,annotations,spring.jcl --module-path /Users/amo/.m2/repository/org/jetbrains/annotations/13.0/annotations-13.0.jar:/Users/amo/.m2/repository/org/aspectj/aspectjweaver/1.8.10/aspectjweaver-1.8.10.jar:/Users/amo/.m2/repository/org/springframework/spring-jcl/5.0.6.RELEASE/spring-jcl-5.0.6.RELEASE.jar /Users/amo/.m2/repository/org/springframework/spring-core/5.0.6.RELEASE/spring-core-5.0.6.RELEASE.jar

jdeps --generate-module-info /Users/amo/Documents/development/vertx/vxms/vxms-demos/vxms-spring-demo/target/moditect   --add-modules aspectjweaver,annotations,spring.jcl --module-path /Users/amo/Documents/development/vertx/vxms/vxms-demos/vxms-spring-demo/target/modules/netty-buffer-4.1.19.Final.jar:/Users/amo/Documents/development/vertx/vxms/vxms-demos/vxms-spring-demo/target/modules/netty-common-4.1.19.Final.jar:/Users/amo/.m2/repository/io/projectreactor/reactor-core/3.1.7.RELEASE/reactor-core-3.1.7.RELEASE.jar:/Users/amo/.m2/repository/org/reactivestreams/reactive-streams/1.0.2/reactive-streams-1.0.2.jar:/Users/amo/.m2/repository/io/reactivex/rxjava2/rxjava/2.1.13/rxjava-2.1.13.jar:/Users/amo/.m2/repository/io/reactivex/rxjava/1.3.8/rxjava-1.3.8.jar:/Users/amo/.m2/repository/io/reactivex/rxjava-reactive-streams/1.2.1/rxjava-reactive-streams-1.2.1.jar:/Users/amo/.m2/repository/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar:/Users/amo/.m2/repository/org/aspectj/aspectjweaver/1.8.10/aspectjweaver-1.8.10.jar:/Users/amo/.m2/repository/org/jetbrains/kotlin/kotlin-reflect/1.2.41/kotlin-reflect-1.2.41.jar:/Users/amo/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.2.41/kotlin-stdlib-1.2.41.jar:/Users/amo/.m2/repository/org/jetbrains/annotations/13.0/annotations-13.0.jar:/Users/amo/.m2/repository/org/springframework/spring-jcl/5.0.6.RELEASE/spring-jcl-5.0.6.RELEASE.jar /Users/amo/.m2/repository/org/springframework/spring-core/5.0.6.RELEASE/spring-core-5.0.6.RELEASE.jar


