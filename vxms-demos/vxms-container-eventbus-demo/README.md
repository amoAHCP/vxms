# vxms container eventbus demo

This applications demonstrates the usage of vxms-rest and vxms-event modules in a container ready application. It consists of 3 sub-projects:

- vxms-frontend is serving the static html pages and is using vxms-rest to create a rest gateway. All rest requests are passed to either the vxms-read or vxms-write project by using events.
- vxms-read is using vxms-event to provide all read methods like "getAllUsers" or "getUserById"
- vxms-write is using vxms-event to provide all CRUD operations using events


## run the application locally

- the application assumes you run mongodb on the same host (default port)
- you can use the the main methods in each project to start the services (multicast must work on your machine)
- OR: you can build the whole project (mvn clean package) and run each service like this: ("java -jar target/*-fat.jar -cluster)
- when the vxms-frontend project is running you can access the application on http://localhost:8181

## run the application in Docker-compose
- stop your local mongodb (port conflicts)
- build the project (mvn clean package)
- build the images (in root the folder of the project type: "docker-compose build")
- run the project by typing "docker-compose up"


## run the application in kubernetes

- deploy a mongoDB (kubectl apply -f kube/)
- deploy the frontend: cd vxms-frontend && mvn clean install fabric8:deploy
- deploy the read: cd vxms-read && mvn clean install fabric8:deploy
- deploy the write: cd vxms-write && mvn clean install fabric8:deploy