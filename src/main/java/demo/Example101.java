package demo;

import io.vertx.core.Vertx;
import io.vertx.rxjava.core.AbstractVerticle;
import rx.Observable;
import rx.observables.MathObservable;

public class Example101 extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Example101.class.getName());
    }

    @Override
    public void start() throws Exception {
        Observable<Integer> obs1 = Observable.range(1, 10);
        Observable<Integer> obs2 = obs1.map(i -> i + 1);
        Observable<Integer> obs3 = obs2.window(2)
            .flatMap(MathObservable::sumInteger);
        obs3.subscribe(
            i -> System.out.println("Computed " + i)
        );
    }
}
