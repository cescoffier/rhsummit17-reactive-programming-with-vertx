package app;

import app.utilities.AuditVerticle;
import app.utilities.Database;
import app.utilities.Product;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import rx.Single;

import static app.utilities.SockJsHelper.getSockJsHandler;

public class App104 extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(App104.class.getName());
        vertx.deployVerticle(AuditVerticle.class.getName());
    }

    private Database database;
    private WebClient pricer;

    @Override
    public void start() throws Exception {
        pricer = WebClient.create(vertx,
            new WebClientOptions().setDefaultPort(8081));

        Router router = Router.router(vertx);
        router.get("/eventbus/*").handler(getSockJsHandler(vertx));
        router.get("/assets/*").handler(StaticHandler.create());
        router.get("/products").handler(this::list);
        router.route().handler(BodyHandler.create());
        router.post("/products").handler(this::add);

        Database.initialize(vertx)
            .flatMap(db -> {
                database = db;
                return vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .rxListen(8080);
            }).subscribe();
    }

    private void add(RoutingContext rc) {
        String name = rc.getBodyAsString().trim();
        database.insert(name)
            .flatMap(p -> {
                Single<Product> price = getPriceForProduct(p);
                Single<Integer> audit = sendActionToAudit(p);
                return Single.zip(price, audit, (pr, a) -> pr);
            })
            .subscribe(
                p -> {
                    String json = Json.encode(p);
                    rc.response().setStatusCode(201).end(json);
                    vertx.eventBus().publish("products", json);
                },
                rc::fail);
    }

    private Single<Integer> sendActionToAudit(Product product) {
        return vertx.eventBus()
            .<Integer>rxSend("audit",
                "Adding " + product.getName())
            .map(Message::body);
    }

    private Single<Product> getPriceForProduct(Product p) {
        return pricer.get("/prices/" + p.getName()).rxSend()
            .map(HttpResponse::bodyAsJsonObject)
            .map(json -> p.setPrice(json.getDouble("price")));
    }

    private void list(RoutingContext rc) {
        HttpServerResponse response = rc.response().setChunked(true);
        database.retrieve()
            .flatMapSingle(p ->
                pricer.get("/prices/" + p.getName())
                    .rxSend()
                    .map(HttpResponse::bodyAsJsonObject)
                    .map(json ->
                        p.setPrice(json.getDouble("price")))
            )
            .subscribe(
                p -> response.write(Json.encode(p) + " \n\n"),
                rc::fail,
                response::end);
    }
}
