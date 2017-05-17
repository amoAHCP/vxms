# Vxms - Kubernetes discovery Demo

In this demo 3 Services will be deployed in Kubernetes. For service deiscoverya Fabric8 based [link](https://github.com/amoAHCP/k8sdiscovery) framework is used.

The frontend contains the UI and acts as a REST gateway. Al get-Requests are passed to the *read* service, the other calls passed to the *write* service.

## how to start
have a running Kubernetes (simply use minikube)
1. start minikube
2. deploy a mongodb (kubectl create -f kube/mongo-controller-no-store.yaml && kubectl create -f kube/mongo-service.yaml)
3. deploy the write service (cd vxms-k8s-write && mvn clean package fabric8:deploy )
4. deploy the read service (cd vxms-k8s-read && mvn clean package fabric8:deploy )
5. deploy the frontend service (cd vxms-k8s-frontend && mvn clean package fabric8:deploy )
6. access the application (minikube service vxms-k8s-frontend --url --> in browser put ip:port/index.html)
