module vxms.rest {
  requires vxms.core;
  requires vertx.core;
  requires vertx.web;
  requires io.netty.codec;
  requires io.netty.codec.http;
  requires java.logging;
  requires java.management;
  requires java.ws.rs;

  exports org.jacpfx.vxms.rest.annotation;
  exports org.jacpfx.vxms.rest.response;
  exports org.jacpfx.vxms.rest.response.basic;
  exports org.jacpfx.vxms.rest.response.blocking;

  provides org.jacpfx.vxms.spi.RESThandlerSPI with
      org.jacpfx.vxms.rest.RESThandler;
}
