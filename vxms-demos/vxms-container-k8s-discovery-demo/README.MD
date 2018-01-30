# vxms kubernetes discovery demo

This applications demonstrates the usage of the vxms-rest module in a Kubernetes/OpenShift application. It consists of 3 sub-projects:

- vxms-frontend is serving the static html pages and acts as a rest gateway. 
  All rest requests are passed to either the vxms-read (@GET) or vxms-write (@POST,@PUT,@DELETE) project. 
  The vxms-frontend (gateway service) is using the vxms-k8sdiscovery module to find the read and the write service by labels (name & version). 
- vxms-read is using vxms-rest to provide all read methods like "getAllUsers" or "getUserById". While maven packaging, the labels "name" and "version" are set from project.artifactId and version.
- vxms-write is using vxms-rest to provide all CRUD operations using events.While maven packaging the labels "name" and "version" are set from project.artifactId and version.




## run the application in Kubernetes / OpenShift
1. when using OpenShift: oc login -u developer -p developer
2. create mongodb service: oc create -f kube/mongoservice.yml 
3. start a mongodb:  oc create -f kube/mongodeployment.yml
4. when using OpenShift: grant rights: oadm policy add-role-to-user view system:serviceaccount:myproject:default -n myproject
5. mvn clean install 
6. when using OpenShift: get service URL: minishift openshift service list -n myproject --> find both frontend service urls
7. verify frontend: curl $ROUTE-URL/index.html
8. verify frontend: curl $ROUTE-URL/index.html (supports only read operations)

### additional notes
- when using Kubernetes, basically replace "oc" with "kubectl"
- in plain kubernetes you do not need to update any policies (step 4)
- in vxms-k8s-frontend/src/main/fabric8/configmap.yml you can set the correct namespace name (in OpenShift the project name)

## run the application locally

- the applicastion assumes you run mongodb on the same host (default port)
- you can use the the main methods in each project to start the services 
- when the vxms-frontend project is running you can access the application on http://localhost:8181

### additional notes
The main method of the VxmsGateway service adds some properties to bypass the Kubernetes discovery locally and to resolve the service ip addresses, 
without changing the annotation properties. Basically you need to create a chain of you annotations key.value pairs, with a value defining the ip 
address locally for the service. 
If you build a fat-jar with maven shade you have to externalize the configuration value to properties or environmental variables. 