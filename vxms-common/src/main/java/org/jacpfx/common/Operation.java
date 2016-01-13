package org.jacpfx.common;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

/**
 * // IDEA... create Optional like compleatablefuture Optional.executeAsync().andThan().get();
 * Created by amo on 27.10.14.
 */
public class Operation implements Serializable{
    private String url;
    private final String type;
    private final String name;
    private final String description;
    private final String[] produces;
    private final String[] consumes;
    private final String[] parameter;
    private transient  Vertx vertx;
    private transient HttpClient client;

    private int connectionPort;
    private String connectionHost;
    private String serviceName;


    public Operation(String url, String type, String[] produces,String[] consumes, String... param) {
       this(url,null,url,type, produces,consumes,param);
    }

    public Operation(String name, String description,String url, String type, String[] produces,String[] consumes, String... param) {
        this(name,description,url,type, produces,consumes,null,param);
    }

    public Operation(String name, String description,String url, String type, String[] produces,String[] consumes, Vertx vertx, String... param) {
        this(name,description,url,type, produces,consumes,null,null,0,vertx,param);
    }

    public Operation(String name, String description,String url, String type, String[] produces,String[] consumes,String serviceName, String connectionHost, int connectionPort,Vertx vertx, String... param) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.type = type;
        this.parameter = param;
        this.produces = produces;
        this.consumes = consumes;
        this.vertx = vertx;
        this.serviceName = serviceName;
        this.connectionHost = connectionHost;
        this.connectionPort = connectionPort;
    }

    public Operation(Operation op,Vertx vertx) {
        this(op.name,op.description,op.url,op.type,op.produces,op.consumes,op.serviceName,op.connectionHost,op.connectionPort,vertx,op.parameter);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }


    public String[] getParameter() {
        return parameter;
    }

    public String[] getProduces() {
        return produces;
    }

    public String[] getConsumes() {
        return consumes;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public int getConnectionPort() {
        return connectionPort;
    }

    public String getConnectionHost() {
        return connectionHost;
    }

    public String getServiceName() {
        return serviceName;
    }


    public boolean isEventBus() {
      return type.equalsIgnoreCase(Type.EVENTBUS.name());
    }

    public boolean isWebSocket() {
        return type.equalsIgnoreCase(Type.WEBSOCKET.name());
    }

    public boolean isREST_GET() {
        return type.equalsIgnoreCase(Type.REST_GET.name());
    }


    /**
     * Returns a http client connected to the router
     * @return
     */
    public Optional<HttpClient> getHttpClient() {
        if(client==null){
            client = vertx.createHttpClient(new HttpClientOptions().
                    setKeepAlive(false));
                   // setDefaultHost(connectionHost).
                   // setDefaultPort(connectionPort));
        }
        return Optional.ofNullable(client);
    }






    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operation)) return false;

        Operation operation = (Operation) o;

        if (url != null ? !url.equals(operation.url) : operation.url != null) return false;
        if (type != null ? !type.equals(operation.type) : operation.type != null) return false;
        if (name != null ? !name.equals(operation.name) : operation.name != null) return false;
        if (description != null ? !description.equals(operation.description) : operation.description != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(produces, operation.produces)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(consumes, operation.consumes)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(parameter, operation.parameter);

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (produces != null ? Arrays.hashCode(produces) : 0);
        result = 31 * result + (consumes != null ? Arrays.hashCode(consumes) : 0);
        result = 31 * result + (parameter != null ? Arrays.hashCode(parameter) : 0);
        return result;
    }


    @Override
    public String toString() {
        return "Operation{" +
                "url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", produces=" + Arrays.toString(produces) +
                ", consumes=" + Arrays.toString(consumes) +
                ", parameter=" + Arrays.toString(parameter) +
                '}';
    }
}
