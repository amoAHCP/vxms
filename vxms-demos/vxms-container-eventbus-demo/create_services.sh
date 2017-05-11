#!/bin/bash
kubectl create -f kube/frontend-service.yaml --namespace=$1
kubectl create -f kube/read-service.yaml --namespace=$1
kubectl create -f kube/write-service.yaml --namespace=$1
