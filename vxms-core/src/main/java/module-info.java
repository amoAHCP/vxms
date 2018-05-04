module vxms.core {
  requires vertx.core;
  requires vertx.web;
  requires java.logging;
  requires java.management;

  exports org.jacpfx.vxms.spi;
  exports org.jacpfx.vxms.common.encoder;
  exports org.jacpfx.vxms.common.decoder;
  exports org.jacpfx.vxms.common.concurrent to vxms.rest,vxms.event, vxms.k8sdiscovery;
  exports org.jacpfx.vxms.common.util to vxms.rest,vxms.event, vxms.k8sdiscovery;
  exports org.jacpfx.vxms.common;
  exports org.jacpfx.vxms.services;
  exports org.jacpfx.vxms.common.throwable;
}