kubectl delete rc/frontend-verticle-controller --namespace=$1
kubectl delete rc/read-verticle-controller --namespace=$1
kubectl delete rc/write-verticle-controller --namespace=$1
