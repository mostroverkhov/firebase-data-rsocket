package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ClientTransportTcp;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class LoggerTest {
    protected Completable serverStop;
    protected Client client;
    protected TestLogger logger;

    @Before
    public void setUp() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        logger = new TestLogger(10_000);
        Server server = new ServerBuilder(
                new ServerTransportTcp(socketAddress))
                .cacheReads()
                .credentialsAuth("creds.properties")
                .logging(logger, new LogConfig.Deployment("localhost", 8090, "1"))
                .build();

        Client client = new ClientBuilder(
                new ClientTransportTcp(socketAddress))
                .build();
        this.client = client;

        serverStop = server.start();
    }

    @Test
    public void readTest() throws Exception {
        ReadRequest readRequest = Requests.readRequest("test", "read")
                .asc()
                .windowWithSize(2)
                .orderByKey()
                .build();
        Flowable<ReadResponse<Data>> dataWindow = client.dataWindow(readRequest, Data.class);
        TestSubscriber<ReadResponse<Data>> testSubscriber = new TestSubscriber<>();
        dataWindow.observeOn(Schedulers.io()).subscribe(testSubscriber);
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        TestSubscriber<Logger.Log.Row> logsTestSubscriber = new TestSubscriber<>();
        logger.log().logFlow().subscribe(logsTestSubscriber);
        logsTestSubscriber.awaitDone(10, TimeUnit.SECONDS);
        List<Logger.Log.Row> values = logsTestSubscriber.values();
        Assert.assertFalse(values.isEmpty());
    }

    @After
    public void tearDown() throws Exception {
        stopServer();
    }

    protected void stopServer() {
        if (serverStop != null) {
            serverStop.toFlowable().subscribe();
            serverStop = null;
        }
    }

    public static class TestLogger implements Logger {

        private final ConcurrentLinkedQueue<Log.Row> rows = new ConcurrentLinkedQueue<>();
        private final int limit;
        private final AtomicBoolean wip = new AtomicBoolean();
        private final AtomicInteger size = new AtomicInteger();

        public TestLogger(int limit) {
            this.limit = limit;
        }

        @Override
        public void log(Log.Row row) {
            rows.offer(row);
            int newSize = size.incrementAndGet();
            if (newSize > limit) {
                boolean start = wip.compareAndSet(false, true);
                if (start) {
                    do {
                        rows.poll();
                    } while (size.decrementAndGet() > limit);
                    wip.set(false);
                }
            }
        }

        @Override
        public Log log() {
            return new Log() {
                @Override
                public Flowable<Row> logFlow() {
                    return Flowable.create(emitter -> {
                        for (Row row : rows) {
                            if (!emitter.isCancelled()) {
                                emitter.onNext(row);
                            }
                        }
                        emitter.onComplete();
                    }, BackpressureStrategy.BUFFER);
                }
            };
        }
    }
}
