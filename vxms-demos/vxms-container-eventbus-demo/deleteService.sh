kubectl delete service/frontend-verticle --namespace=$1
kubectl delete service/read-verticle --namespace=$1
kubectl delete service/write-verticle --namespace=$1
