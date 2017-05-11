#!/bin/bash
kubectl create -f kube/frontend-controller.yaml --namespace=$1
kubectl create -f kube/read-controller.yaml --namespace=$1
kubectl create -f kube/write-controller.yaml --namespace=$1
