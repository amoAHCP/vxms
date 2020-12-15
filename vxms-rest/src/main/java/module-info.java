import org.jacpfx.vxms.rest.RestBaseHandler;

module vxms.rest {
  requires vxms.core;
  requires io.vertx.core;
  requires io.vertx.web;
  requires io.netty.codec;
  requires io.netty.codec.http;
  requires java.logging;
  requires java.management;
  requires vxms.rest.base;

  provides org.jacpfx.vxms.spi.RESThandlerSPI with
          RestBaseHandler;
}
