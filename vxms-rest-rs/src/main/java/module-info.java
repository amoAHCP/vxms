import org.jacpfx.vxms.rest.RestRsHandler;

module vxms.rest.rs {
  requires vxms.core;
  requires vertx.core;
  requires vertx.web;
  requires io.netty.codec;
  requires io.netty.codec.http;
  requires java.logging;
  requires java.management;
  requires java.ws.rs;

  requires vxms.rest.base;
  uses org.jacpfx.vxms.spi.RESThandlerSPI;
  provides org.jacpfx.vxms.spi.RESThandlerSPI with
          RestRsHandler;
}
