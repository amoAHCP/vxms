package org.jacpfx.vertx.rest.response;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class RSRequest {

    private final RoutingContext context;

    public RSRequest(RoutingContext context) {
        this.context = context;
    }

    public String param(String paramName) {
        return context.request().getParam(paramName);
    }

    public String header(String headerName) {
        return context.request().getHeader(headerName);
    }

    public String formAttribute(String attributeName) {
        return context.request().getFormAttribute(attributeName);
    }

    public Set<FileUpload> fileUploads() {
        return context.fileUploads();
    }

    public Set<Cookie> cookies() {
        return context.cookies();
    }

    public Buffer body() {
        return context.getBody();
    }


}
