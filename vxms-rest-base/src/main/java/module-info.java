module vxms.rest.base {
    requires vxms.core;
    requires io.vertx.core;
    requires io.vertx.web;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires java.logging;
    requires java.management;


    exports org.jacpfx.vxms.rest.base;
    exports org.jacpfx.vxms.rest.base.annotation;
    exports org.jacpfx.vxms.rest.base.response;
    exports org.jacpfx.vxms.rest.base.response.basic;
    exports org.jacpfx.vxms.rest.base.response.blocking;
    exports org.jacpfx.vxms.rest.base.util;

}
