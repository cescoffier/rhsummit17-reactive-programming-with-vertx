package app.utilities;

import io.vertx.core.buffer.Buffer;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RandomIntegerPublisher implements Publisher<Integer> {

    private Random random = new Random();
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void subscribe(Subscriber<? super Integer> s) {
        Subscription subscription = new Subscription() {
            private volatile boolean cancelled;

            @Override
            public void request(long n) {
                service.schedule(() -> {
                    if (! cancelled) {
                        int next = random.nextInt(10);
                        s.onNext(next);
                    }
                }, 1, TimeUnit.SECONDS);
            }

            @Override
            public void cancel() {
               cancelled = true;
            }
        };
        s.onSubscribe(subscription);
    }
}
