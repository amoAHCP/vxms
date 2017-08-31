# configuration flags
Can be set, by using a property json file or by setting System env variable
 
 
 
 | property name |  description                        |  default| possible vaslues |
 |--- |---|---|---|
 | name  |  the name/identifier of the service  | --- |
 | port          |  the port number to bind http socket |  8080 |
 | host          |  the host name/interface to bind to  | 0.0.0.0 |
 | contextRoot  |  the context-route for your service  | "/" |
 | cbScope  |  The Scope defines whether the stateful circuit breaker is global (cluster wide), local (jvm wide) or unique (instance wide) | unique | global, local, unique|
 

 