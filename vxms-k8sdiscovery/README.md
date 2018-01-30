# vxms-k8s-discovery module

This modules allows extended Kubernetes service discovery in Vxms REST environments. 
It will discover the Service IP address by name and/or labels and also by defined Ports of your Kubernetes Service. The discovery is not restricted to vxms / vert.x and is able to discovery any type of Kubernetes service.

## What it is not
This discovery module is not a replacement for the **"-cluster"** mode of Vert.x. If you want to use a clustered event-bus you have to use the Hazelcast/Kubernetes Plugin.

## What is the advantage
To discover Services in Kubernetes you typically can relay on DNS or on Environmental variables. When using DNS, the Service name will be resolved to an IP Address in Kubernetes. The vxms K8S module currently supports service discovery using a Service name or Labels. 
When using discovery by service names (and by Labels too), you profit from the "fall-back" mechanism in vxms which enables you to run your services locally. In this case you can resolve the service names using properties, so you don't have to change the application code 
when running in Kubernetes or locally. This discovery module can also resolve a specific Port, defined in your Kubernetes Service.  
The discovery using Labels, goes beyond the "normal" discovery mechanism of Kubernetes. Each Kubernetes Service descriptor can define many Labels, those Labels can be used to discover a specific Service.


## Usage

add the following dependency:
```xml 
  
  <dependency>
       <groupId>org.jacpfx</groupId>
       <artifactId>vxms-k8sdiscovery</artifactId>
       <version>${version}</version>
  </dependency>
``` 

and following Class annotation
```java 
  @K8SDiscovery
  public class VxmsGateway extends VxmsEndpoint {}
``` 

### What will be resolved
The result of the discovery is a string with IP:PORT

### Configuring the connection to the Kubernetes master
Without any additional configuration, the discovery should work in a pure Kurbenetes environment (namespace = "default") out-of-the-box. There are several ways to configure the Kubernetes environment.

#### configuration, using the Annotation **@K8SDiscovery**
The annotation @K8SDiscovery provides following attributes to configure the access to the Kubernetes master:

- user: the user to access the master --> default: empty
- password: the password of the user to access the master --> default: empty
- api_token: the api token to access the master --> default: empty, the access token is read by default from the container
- master_url: the URL of the Kubernetes master --> default: https://kubernetes.default.svc
- namespace: the namespace, where to do the discovery. For security reasons, the discovery is restricted to one namespace --> default: default
- customClientConfiguration: a Class implementing the org.jacpfx.vxms.k8s.api.CustomClientConfig Interface. This allows you to define a more detailed configuration using Fabric8s io.fabric8.kubernetes.client.Config Class.

All attributes from the annotation can be overwritten using properties or environmental variables. The properties have the same key-name as the attributes, where as the environmental variables are in capital letters.

#### advanced configuration
If the configuration attributes from the @K8SDiscovery annotation are not enough, you can provide an implementation of the interface org.jacpfx.vxms.k8s.api.CustomClientConfig. 
The interfaces defines a method to implement, passing the Vert.x instance (access to properties) and returning an instance of io.fabric8.kubernetes.client.Config.
This configuration class provides additional ways to configure the access to the Kubernetes master. To register this custom configuration, declare the class in the *customClientConfiguration* attribute of the @K8SDiscovery annotation, 
or user properties (or environmental variables) to define the class.

### discovery using Service name only
```java 
  @ServiceName("readService")
  private String readService;
``` 
you can also use a property placeholder like this:
```java 
  @ServiceName("${myService}")
  private String readService;
``` 

### discovery using Service name and Port name
```java 
  @ServiceName("readService")
  @PortName("http")
  private String readService;
``` 
you can also use a property placeholder like this:
```java 
  @ServiceName("${myService}")
  @PortName("${myService.port}")
  private String readService;
``` 

### discovery using Labels
When using discovery by labels, the annotation @ServiceName is still needed (because later it is planned to do discovery of Pods too), but the Value will be ignored, so you can leave it.
```java 
  @ServiceName
  @WithLabel(name = "name", value = "${read_name}")
  private String readService;
``` 


### discovery using many Labels 

When using discovery by labels, the annotation @ServiceName is still needed (because later it is planned to do discovery of Pods too), but the Value will be ignored, so you can leave it.
```java 
  @ServiceName
   @WithLabels({
      @WithLabel(name = "name", value = "${read_name}"),
      @WithLabel(name = "version", value = "${read_version}")
    })
  private String readService;
``` 
you can also use a property placeholder like this:
```java 
  @ServiceName
   @WithLabels({
      @WithLabel(name = "name", value = "${read_name}"),
      @WithLabel(name = "version", value = "${read_version}")
    })
  private String readService;
``` 

### discovery using Labels and Port names
```java 
  @ServiceName
   @WithLabels({
      @WithLabel(name = "name", value = "${read_name}"),
      @WithLabel(name = "version", value = "${read_version}")
    })
  @PortName("${myService.port}")
  private String readService;
``` 


## Properties and environmental configuration
All discovery annotations like @ServiceName, @WithLabel and @PortName supporting placeholders. Placeholders starts with “${" and ends with "}". 
The placeholder itself can be resolved using the default Vert.x config/property mechanism, or using environmental variables.


## Local usage

Let's assume following configuration:
```java 
 @ServiceName()
  @WithLabels({
    @WithLabel(name = "name", value = "${read_name}"),
    @WithLabel(name = "version", value = "${read_version}")
  })
  private String read;
``` 
We have two key/value pairs defining the Service to discover in Kubernetes. The idea of "local" discovery is, 
1.) provide a hint that we are running locally
  - by providing the configuration hint "new JsonObject().put("kube.offline", true)", no connection to the Kubernetes Master will be established
2.) resolve the key/value pairs to point to a local address
  - new JsonObject().put("read_name", "vxms-k8s-read")
                        .put("read_version", "1.1-SNAPSHOT")
                        .put("write_name", "vxms-k8s-write")
                        .put("write_version", "1.1-SNAPSHOT")
                        .put("name.vxms-k8s-read.version.1.1-SNAPSHOT", "localhost:7070")
                        .put("name.vxms-k8s-write.version.1.1-SNAPSHOT", "localhost:9090"));
  - as you can see, the first step is to resolve the placeholders (${read_name} & ${read_version}), the next step is to concat the compleate Label definition and to point to a local address (key1.value1.key2.value2 ... and so on)                      
## examples
[vxms-container-k8s-discovery-demo](https://github.com/amoAHCP/vxms/tree/master/vxms-demos/vxms-container-k8s-discovery-demo)