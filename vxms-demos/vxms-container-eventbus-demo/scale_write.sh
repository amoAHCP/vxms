#!/bin/bash
kubectl scale rc write-verticle-controller --replicas=$1 --namespace=$2
