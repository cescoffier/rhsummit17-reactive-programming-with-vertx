package app.utilities;

import io.vertx.core.Handler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SockJsHelper {

    public static Handler<RoutingContext> getSockJsHandler(Vertx vertx) {
        SockJSHandler sockJSHandler = SockJSHandler
            .create(vertx);
        BridgeOptions options = new BridgeOptions();
        options.addInboundPermitted(
            new PermittedOptions().setAddress("products"));
        options.addInboundPermitted(
            new PermittedOptions().setAddress("random"));
        options.addOutboundPermitted(
            new PermittedOptions().setAddress("products"));
        options.addOutboundPermitted(
            new PermittedOptions().setAddress("random"));
        sockJSHandler.bridge(options);
        return sockJSHandler;
    }
}
