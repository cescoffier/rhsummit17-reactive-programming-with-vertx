package app.utilities;

import io.vertx.rxjava.core.Context;
import io.vertx.rxjava.core.Vertx;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RandomIntegerSubscriber implements Subscriber<Integer> {

    private final Vertx vertx;

    private Subscription subscription;
    private volatile boolean stopped;

    public RandomIntegerSubscriber(Vertx vertx) {
        this.vertx = vertx;
    }

    private synchronized void terminate() {
        stopped = true;
        if (subscription != null) {
            subscription.cancel();
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        subscription.request(1);
    }

    @Override
    public void onNext(Integer integer) {
        if (stopped) {
            return;
        }
        vertx.eventBus().publish("random", integer.toString());
        vertx.setTimer(3000, l -> subscription.request(1));
    }

    @Override
    public void onError(Throwable t) {
        terminate();
    }

    @Override
    public void onComplete() {
        terminate();
    }
}
