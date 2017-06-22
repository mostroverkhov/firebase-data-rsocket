package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.notification.NotificationTransformer;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.read.DataWindowTransformer;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ClientTransportTcp;
import com.github.mostroverkhov.firebase_rsocket.transport.tcp.ServerTransportTcp;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.TypedReadResponse;
import com.google.gson.Gson;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class LoggerTest {
    private static final Gson gson = new Gson();
    protected DataWindowTransformer<Data> dataWindowTransformer;
    protected NotificationTransformer<Data> notifTransformer;

    protected Completable serverStop;
    protected Client client;
    protected TestLogger logger;

    @Before
    public void setUp() throws Exception {
        dataWindowTransformer = new DataWindowTransformer<>(gson, Data.class);
        notifTransformer = new NotificationTransformer<>(gson, Data.class);

        InetSocketAddress socketAddress = new InetSocketAddress(8090);
        logger = new TestLogger(10_000);
        Server server = new ServerBuilder(
                new ServerTransportTcp(socketAddress))
                .cacheReads()
                .classpathPropsAuth("creds.properties")
                .logging(logger)
                .build();

        ClientFactory clientFactory = new ClientFactoryBuilder(
                new ClientTransportTcp(socketAddress))
                .build();
        this.client = clientFactory.client(Client.class);

        serverStop = server.start();
    }

    @Test
    public void readTest() throws Exception {
        ReadRequest readRequest = Requests.read("test", "read")
                .asc()
                .windowWithSize(2)
                .orderByKey()
                .build();
        Flowable<TypedReadResponse<Data>> dataWindow = client.dataWindow(readRequest)
                .flatMap(dataWindowTransformer::apply);
        TestSubscriber<TypedReadResponse<Data>> testSubscriber = new TestSubscriber<>();
        dataWindow.observeOn(Schedulers.io()).subscribe(testSubscriber);
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        Queue<Logger.Row> logRows = logger.rows();
        Assert.assertFalse(logRows.isEmpty());
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

        private final Queue<Row> rows = new ConcurrentLinkedQueue<>();
        private final int limit;
        private final AtomicBoolean wip = new AtomicBoolean();
        private final AtomicInteger size = new AtomicInteger();

        public TestLogger(int limit) {
            this.limit = limit;
        }

        @Override
        public void log(Row row) {
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

        public Queue<Row> rows() {
            return rows;
        }
    }
}
