#!/bin/bash
PROJECT_NAME=kubernetestests-147607
IMAGE_VERSION=v1
DEPLOY=false


for i in "$@"
do
case $i in
    -v=*|--version=*)
    IMAGE_VERSION="${i#*=}"

    ;;
    -p=*|--projectName=*)
    PROJECT_NAME="${i#*=}"
    ;;
    -d=*|--deploy=*)
    DEPLOY="${i#*=}"
    ;;
    *)
    ;;
esac
done

docker build -t gcr.io/$PROJECT_NAME/vxms-write:$IMAGE_VERSION vxms-write/

if $DEPLOY; then
    gcloud docker push gcr.io/$PROJECT_NAME/vxms-write:$IMAGE_VERSION
fi
