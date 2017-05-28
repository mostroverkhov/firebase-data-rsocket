package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class LatencyCheck extends AbstractTest {

    private static final int REQUEST_N = 1;
    private static final int SUBSCRIBERS_COUNT = 1000;

    @Ignore
    @Test
    public void read() throws Exception {

        ReadRequest readRequest = requestStreamRequest();
        Flowable<ReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest, Data.class)
                .repeatWhen(completed -> completed.flatMap(Flowable::just))
                .observeOn(Schedulers.io());

        ConcurrentHistogram histogram = new ConcurrentHistogram(20000, 1);
        histogram.setAutoResize(true);
        Recorder recorder = new Recorder(histogram);

        List<TestSubscriber<ReadResponse<Data>>> subscribers = subscribers(SUBSCRIBERS_COUNT, recorder);
        TestSubscriber<ReadResponse<Data>> firstSubs = subscribers.get(0);

        subscribers.forEach(dataWindowFlow::subscribe);
        firstSubs.awaitDone(15, TimeUnit.SECONDS);
        subscribers.forEach(TestSubscriber::cancel);
        histogram.outputPercentileDistribution(System.out, 1d);
    }

    @Override
    public void tearDown() throws Exception {
        stopServerDelayed(100, TimeUnit.MILLISECONDS);
    }

    private List<TestSubscriber<ReadResponse<Data>>> subscribers(int count, Recorder recorder) {
        List<TestSubscriber<ReadResponse<Data>>> subscribers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            subscribers.add(testSubscriber(recorder));
        }
        return subscribers;
    }


    private TestSubscriber<ReadResponse<Data>> testSubscriber(Recorder recorder) {
        return new TestSubscriber<ReadResponse<Data>>(REQUEST_N) {
            @Override
            public void onNext(ReadResponse<Data> o) {
                super.onNext(o);
                recorder.record();
                request(REQUEST_N);
            }
        };
    }

    private static class Recorder {
        private final Histogram histogram;
        private volatile long from;
        private AtomicInteger wip = new AtomicInteger();
        private final Queue<FromTo> queue = new ConcurrentLinkedQueue<>();

        public Recorder(Histogram histogram) {
            this.histogram = histogram;
        }

        public void record() {
            long fr = from;
            long to = System.currentTimeMillis();
            from = to;
            FromTo fromTo = new FromTo(fr, to);
            queue.offer(fromTo);
            if (wip.getAndIncrement() == 0) {
                do {
                    FromTo peek = queue.poll();
                    if (peek.getFrom() != 0) {
                        histogram.recordValue(peek.getTo() - peek.getFrom());
                    }
                } while (wip.decrementAndGet() != 0);
            }
        }

        private static class FromTo {
            private final long from;
            private final long to;

            public FromTo(long from, long to) {
                this.from = from;
                this.to = to;
            }

            public long getFrom() {
                return from;
            }

            public long getTo() {
                return to;
            }
        }
    }

    private ReadRequest requestStreamRequest() {
        return Requests
                .read("test", "read")
                .asc()
                .windowWithSize(2)
                .orderByKey()
                .build();
    }
}
