# vxms Kubernetes discovery
This module provides an annotation-based discovery of Kubernetes Services by name and by labels (for REST Services). In version 1.1 only discovering Services is supported, later a discovery for Pods and Endpoints will be added.

## basic idea
Kubernetes provides 2 basic ways of doing Service discovery. Discovery using DNS (resolving the service name) or discovery using Environmental variables