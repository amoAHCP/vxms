# configuration flags
Can be set by using a property json file or by setting System env variable
 
 
 
 | property name |  description                        |
 |--- |---|
 | service-name  |  the name/identifier of the service  |
 | port          |  the port number to bind http socket         |
 | host          |  the host name/interface to bind to          |
 | context-root  |  the context-route for your service  |
 | exportedHost  |  the host name used for service discovery, this exportedHost is useful when working with kubernetes and other orchestration tools                               |
 | exportedPort  |  the port used for service discovery, this exportedPort is useful when working with kubernetes and other orchestration tools                               |
 |               |                                     |
 | etcdhost      |  host name of the etcd client to connect |
 | etcdport      |  the port number of the etcd client to connect |
 |  ttl          |  the time to live amount, defines how long a service registration is valid in etcd (before refreshed by default) |
 |  domain       | the domain of your service (services can be grouped in a domain)  |
 |               |                          |
 

 