#!/bin/bash
kubectl scale rc read-verticle-controller --replicas=$1 --namespace=$2
