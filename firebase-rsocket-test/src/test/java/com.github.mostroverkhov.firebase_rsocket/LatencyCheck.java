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

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class LatencyCheck extends AbstractTest {

    private static final int REQUEST_N = 1;

    @Ignore
    @Test
    public void read() throws Exception {
        ReadRequest readRequest = requestStreamRequest();
        Flowable<ReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest, Data.class);

        ConcurrentHistogram histogram = new ConcurrentHistogram(20000, 1);
        histogram.setAutoResize(true);
        Recorder recorder = new Recorder(histogram);

        TestSubscriber<ReadResponse<Data>> testSubscriber
                = new TestSubscriber<ReadResponse<Data>>(REQUEST_N) {
            @Override
            public void onNext(ReadResponse<Data> o) {
                super.onNext(o);
                recorder.record();
                request(REQUEST_N);
            }
        };

        dataWindowFlow
                .repeatWhen(completed -> completed.flatMap(Flowable::just))
                .observeOn(Schedulers.io())
                .subscribe(testSubscriber);

        testSubscriber.awaitDone(30, TimeUnit.SECONDS);
        testSubscriber
                .assertNoErrors();

        histogram.outputPercentileDistribution(System.out, 1d);
    }

    private static class Recorder {
        private final Histogram histogram;
        private volatile long from;

        public Recorder(Histogram histogram) {
            this.histogram = histogram;
        }

        public void record() {
            long to = System.currentTimeMillis();
            if (from != 0) {
                histogram.recordValue(to - from);
            }
            from = to;
        }
    }

    private ReadRequest requestStreamRequest() {
        return Requests
                .readRequest("test", "read")
                .asc()
                .windowWithSize(2)
                .orderByKey()
                .build();
    }
}
