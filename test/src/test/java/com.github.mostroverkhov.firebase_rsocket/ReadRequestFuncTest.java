package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.api.Requests;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.TypedNotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.TypedReadResponse;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class ReadRequestFuncTest extends AbstractTest {

    private static final int SAMPLE_ITEM_COUNT = 10;
    private static final int WINDOW_SIZE = 2;
    private static final int REQUEST_N = 1;
    private static final int READ_REPEAT_N = 3;

    @Test
    public void presentRead() throws Exception {

        ReadRequest readRequest = presentReadRequest();
        Flowable<TypedReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest)
                .flatMap(dataWindowTransformer::from);
        TestSubscriber<TypedReadResponse<Data>> testSubscriber
                = requestStreamSubscriber();

        dataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(testSubscriber);

        testSubscriber.awaitDone(20, TimeUnit.SECONDS);
        int valueCount = SAMPLE_ITEM_COUNT / WINDOW_SIZE;
        testSubscriber
                .assertNoErrors()
                .assertValueCount(valueCount);

        for (int i = 0; i < valueCount; i++) {
            int c = i;
            testSubscriber.assertValueAt(i,
                    window -> window.getData().size() == 2);
            testSubscriber.assertValueAt(i,
                    window -> assertWindowContent(window, c));
        }
    }

    @Test
    public void presentReadStartWith() throws Exception {

        ReadRequest allRequest = presentReadRequest();
        Flowable<TypedReadResponse<Data>> allDataWindowFlow = client
                .dataWindow(allRequest)
                .flatMap(dataWindowTransformer::from);
        TestSubscriber<TypedReadResponse<Data>> allDatatestSubscriber
                = requestStreamSubscriber();

        allDataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(allDatatestSubscriber);

        allDatatestSubscriber.awaitDone(20, TimeUnit.SECONDS);
        int valueCount = SAMPLE_ITEM_COUNT / WINDOW_SIZE;

        ReadRequest tailRequest = Requests
                .read("test", "read")
                .asc()
                .windowWithSize(WINDOW_SIZE)
                .orderByKey()
                .startWith(lastWindowStartKey(allDatatestSubscriber, valueCount))
                .build();

        Flowable<TypedReadResponse<Data>> tailDataWindowFlow = client
                .dataWindow(tailRequest).flatMap(dataWindowTransformer::from);
        TestSubscriber<TypedReadResponse<Data>> tailTestSubscriber
                = requestStreamSubscriber();

        tailDataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(tailTestSubscriber);

        tailTestSubscriber.awaitDone(20, TimeUnit.SECONDS);
        tailTestSubscriber
                .assertNoErrors()
                .assertValueCount(1);
        tailTestSubscriber.assertValueAt(0,
                window -> window.getData().size() == 2);
    }

    @Test
    public void missingRead() throws Exception {

        ReadRequest readRequest = missingReadRequest();
        Flowable<TypedReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest).flatMap(dataWindowTransformer::from);
        TestSubscriber<TypedReadResponse<Data>> testSubscriber
                = requestStreamSubscriber();

        dataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(testSubscriber);

        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber
                .assertNoErrors()
                .assertComplete()
                .assertValueCount(0);
    }

    @Test
    public void presentReadRepeat() throws Exception {

        ReadRequest readRequest = presentReadRequest();
        Flowable<TypedReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest).flatMap(dataWindowTransformer::from);
        TestSubscriber<TypedReadResponse<Data>> testSubscriber
                = requestStreamSubscriber();

        dataWindowFlow
                .repeat(READ_REPEAT_N)
                .observeOn(Schedulers.io())
                .subscribe(testSubscriber);

        testSubscriber.awaitDone(30, TimeUnit.SECONDS);
        int valueCount = READ_REPEAT_N * SAMPLE_ITEM_COUNT / WINDOW_SIZE;
        testSubscriber
                .assertNoErrors()
                .assertValueCount(valueCount);
    }

    @Test
    public void presentReadNotifications() throws Exception {

        ReadRequest readRequest = presentReadRequest();
        Flowable<TypedNotifResponse<Data>> notifFlow = client
                .dataWindowNotifications(readRequest)
                .flatMap(notifTransformer::from);
        TestSubscriber<TypedNotifResponse<Data>> testSubscriber = TestSubscriber.create();
        notifFlow.observeOn(Schedulers.io())
                .subscribe(testSubscriber);
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotComplete();
        List<TypedNotifResponse<Data>> events = testSubscriber.values();
        Assert.assertEquals(3, events.size());
        List<TypedNotifResponse<Data>> changeEvents = changeEvents(events);

        List<Data> dataList = changeEvents.stream().map(TypedNotifResponse::getItem)
                .collect(Collectors.toList());
        Assert.assertTrue(contains(dataList, new Data("0", "0")));
        Assert.assertTrue(contains(dataList, new Data("1", "1")));

        Optional<TypedNotifResponse<Data>> maybeNextWindow = nextWindowEvent(events);
        Assert.assertTrue(maybeNextWindow.isPresent());
        ReadRequest nextRead = maybeNextWindow.get().getNextDataWindow();
        Assert.assertNotNull(nextRead);
        Assert.assertNotNull(nextRead.getWindowStartWith());
    }

    @Test
    public void presentReadNotificationsChained() throws Exception {

        ReadRequest readRequest = presentReadRequest();
        Flowable<TypedNotifResponse<Data>> notifFlow = client
                .dataWindowNotifications(readRequest).flatMap(notifTransformer::from);
        TestSubscriber<TypedNotifResponse<Data>> testSubscriber = TestSubscriber.create();
        notifFlow.observeOn(Schedulers.io())
                .filter(TypedNotifResponse::isNextWindow)
                .map(TypedNotifResponse::getNextDataWindow)
                .flatMap(req -> client
                        .dataWindowNotifications(req)
                        .flatMap(notifTransformer::from))
                .subscribe(testSubscriber);

        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotComplete();
        List<TypedNotifResponse<Data>> events = testSubscriber.values();
        Assert.assertEquals(3, events.size());
        Optional<TypedNotifResponse<Data>> maybeNextWindow = nextWindowEvent(events);

        Assert.assertTrue(maybeNextWindow.isPresent());
        ReadRequest nextRead = maybeNextWindow.get().getNextDataWindow();

        List<TypedNotifResponse<Data>> changeEvents = changeEvents(events);
        List<Data> dataList = changeEvents.stream().map(TypedNotifResponse::getItem)
                .collect(Collectors.toList());
        Assert.assertTrue(contains(dataList, new Data("2", "2")));
        Assert.assertTrue(contains(dataList, new Data("3", "3")));

        Assert.assertNotNull(nextRead);
        Assert.assertNotNull(nextRead.getWindowStartWith());
    }

    @Test
    public void absentReadNotifications() throws Exception {

        ReadRequest readRequest = missingReadRequest();
        Flowable<TypedNotifResponse<Data>> notifFlow = client
                .dataWindowNotifications(readRequest)
                .flatMap(notifTransformer::from);
        TestSubscriber<TypedNotifResponse<Data>> testSubscriber = TestSubscriber.create();
        notifFlow.observeOn(Schedulers.io())
                .subscribe(testSubscriber);
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotComplete();
        List<TypedNotifResponse<Data>> events = testSubscriber.values();
        Assert.assertEquals(1, events.size());
        TypedNotifResponse<Data> dataItem = events.get(0);
        Assert.assertTrue(dataItem.isNextWindow());
        Assert.assertTrue((dataItem.getNextDataWindow().getWindowStartWith().isEmpty()));
    }

    private <T> boolean contains(List<T> items, T item) {
        for (T t : items) {
            if (t.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private <T> Optional<TypedNotifResponse<T>> nextWindowEvent(List<TypedNotifResponse<T>> notifResponses) {

        for (TypedNotifResponse<T> resp : notifResponses) {
            if (resp.isNextWindow()) {
                return Optional.of(resp);
            }
        }
        return Optional.empty();
    }

    private <T> List<TypedNotifResponse<T>> changeEvents(List<TypedNotifResponse<T>> notifResponses) {
        List<TypedNotifResponse<T>> res = new ArrayList<>();
        for (TypedNotifResponse<T> resp : notifResponses) {
            if (resp.isChangeEvent()) {
                res.add(resp);
            }
        }
        return res;
    }

    private String lastWindowStartKey(TestSubscriber<TypedReadResponse<Data>> testSubscriber,
                                      int valueCount) {
        return testSubscriber
                .values()
                .get(valueCount - 1)
                .getReadRequest()
                .getWindowStartWith();
    }

    private ReadRequest presentReadRequest() {
        return Requests
                .read("test", "read")
                .asc()
                .windowWithSize(WINDOW_SIZE)
                .orderByKey()
                .build();
    }

    private ReadRequest missingReadRequest() {
        return Requests
                .read("foo", "bar")
                .asc()
                .windowWithSize(WINDOW_SIZE)
                .orderByKey()
                .build();
    }


    private TestSubscriber<TypedReadResponse<Data>> requestStreamSubscriber() {
        return new TestSubscriber<TypedReadResponse<Data>>(REQUEST_N) {
            @Override
            public void onNext(TypedReadResponse<Data> o) {
                super.onNext(o);
                request(REQUEST_N);
            }
        };
    }

    private boolean assertWindowContent(TypedReadResponse<Data> window, int index) {
        int doubledIndex = index * 2;
        for (Data data : window.getData()) {
            if (!String.valueOf(doubledIndex).equals(data.getId())) {
                return false;
            }
            doubledIndex++;
        }
        return true;
    }
}
