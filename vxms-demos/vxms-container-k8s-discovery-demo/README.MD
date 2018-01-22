# Vxms - Kubernetes discovery Demo

The frontend contains the UI and acts as a REST gateway. Al get-Requests are passed to the *read* service, the other calls passed to the *write* service.

## how to start
have a running Kubernetes (simply use minikube)
1. start minikube/minishift (and login (oc login) if minishift  )
2. deploy a mongodb (kubectl create -f kube/mongo-controller-no-store.yaml && kubectl create -f kube/mongo-service.yaml)
3. deploy the write service (cd vxms-k8s-write && mvn clean package fabric8:deploy )
4. deploy the read service (cd vxms-k8s-read && mvn clean package fabric8:deploy )
5. when using OpenShift/minishift -->  grant rights: oadm policy add-role-to-user view system:serviceaccount:myproject:default -n myproject
6. deploy the frontend service (cd vxms-k8s-frontend && mvn clean package fabric8:deploy )
7. access the application (minikube service vxms-k8s-frontend --url --> in browser put ip:port/index.html)


## using it locally
The vxms-k8sdiscovery modules provides an easy way to run your application in kubernetes as well as locally


