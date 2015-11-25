package org.jacpfx.common;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The ServiceInforResult returns a stream of serviceInfo references. It will be returned by the ServiceDiscovery handler when looking for a specific service.
 * Created by Andy Moncsek on 05.05.15.
 */
public class ServiceInfoResult {

    private final Stream<ServiceInfo> serviceInfoStream;
    private final boolean succeeded;
    private final Throwable cause;


    /**
     * The default constructor
     *
     * @param serviceInfoStream The stream of ServiceInfos found
     * @param succeeded         the connection status
     * @param cause             The failure caus
     */
    public ServiceInfoResult(Stream<ServiceInfo> serviceInfoStream, boolean succeeded, Throwable cause) {
        this.serviceInfoStream = serviceInfoStream;
        this.succeeded = succeeded;
        this.cause = cause;
    }

    /**
     * The stream of services found
     *
     * @return a stream with requested ServiceInfos
     */
    public Stream<ServiceInfo> getServiceInfoStream() {
        return serviceInfoStream;
    }

    /**
     * The ServiceInfo
     * @return Returns the  serviceInfo
     */
    public ServiceInfo getServiceInfo() { return serviceInfoStream.findFirst().get();}



    /**
     * The connection status
     *
     * @return true if connection to ServiceRepository succeeded
     */
    public boolean succeeded() {
        return succeeded;
    }

    /**
     * The connection status
     *
     * @return true if connection to ServiceRepository NOT succeeded
     */
    public boolean failed() {
        return !succeeded;
    }

    /**
     * Returns the failure cause
     *
     * @return The failure cause when connection to ServiceRegistry was not successful
     */
    public Throwable getCause() {
        return cause;
    }

    public static Consumer<ServiceInfoResult> onSuccessService(Consumer<ServiceInfo> consumer, Consumer<Throwable> onFail) {
        return result -> {
            if (result.failed()) {
                onFail.accept(result.getCause());
            } else {
                consumer.accept(result.getServiceInfo());
            }
        };
    }

}
