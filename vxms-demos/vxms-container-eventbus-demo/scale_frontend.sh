#!/bin/bash
kubectl scale rc frontend-verticle-controller --replicas=$1 --namespace=$2
