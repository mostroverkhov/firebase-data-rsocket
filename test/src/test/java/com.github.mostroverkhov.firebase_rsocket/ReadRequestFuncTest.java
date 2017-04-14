package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.DataWindowChangeEvent;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.DataWindowNotif;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NextWindow;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications.NotifResponse;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
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
        Flowable<ReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest, Data.class);
        TestSubscriber<ReadResponse<Data>> testSubscriber
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
        Flowable<ReadResponse<Data>> allDataWindowFlow = client
                .dataWindow(allRequest, Data.class);
        TestSubscriber<ReadResponse<Data>> allDatatestSubscriber
                = requestStreamSubscriber();

        allDataWindowFlow
                .observeOn(Schedulers.io())
                .subscribe(allDatatestSubscriber);

        allDatatestSubscriber.awaitDone(20, TimeUnit.SECONDS);
        int valueCount = SAMPLE_ITEM_COUNT / WINDOW_SIZE;

        ReadRequest tailRequest = Requests
                .readRequest("test", "read")
                .asc()
                .windowWithSize(WINDOW_SIZE)
                .orderByKey()
                .startWith(lastWindowStartKey(allDatatestSubscriber, valueCount))
                .build();

        Flowable<ReadResponse<Data>> tailDataWindowFlow = client
                .dataWindow(tailRequest, Data.class);
        TestSubscriber<ReadResponse<Data>> tailTestSubscriber
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
        Flowable<ReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest, Data.class);
        TestSubscriber<ReadResponse<Data>> testSubscriber
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
        Flowable<ReadResponse<Data>> dataWindowFlow = client
                .dataWindow(readRequest, Data.class);
        TestSubscriber<ReadResponse<Data>> testSubscriber
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
        Flowable<NotifResponse> notifFlow = client
                .dataWindowNotifications(readRequest, Data.class);
        TestSubscriber<NotifResponse> testSubscriber = TestSubscriber.create();
        notifFlow.observeOn(Schedulers.io())
                .subscribe(testSubscriber);
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotComplete();
        List<NotifResponse> events = testSubscriber.values();
        Assert.assertEquals(3, events.size());
        List<DataWindowChangeEvent<Data>> changeEvents = changeEvents(events);

        List<Data> dataList = changeEvents.stream().map(DataWindowChangeEvent::getItem)
                .collect(Collectors.toList());
        Assert.assertTrue(contains(dataList, new Data("0", "0")));
        Assert.assertTrue(contains(dataList, new Data("1", "1")));

        Optional<NextWindow> maybeNextWindow = nextWindowEvent(events);
        Assert.assertTrue(maybeNextWindow.isPresent());
        ReadRequest nextRead = maybeNextWindow.get().getNext();
        Assert.assertNotNull(nextRead);
        Assert.assertNotNull(nextRead.getWindowStartWith());
    }

    @Test
    public void presentReadNotificationsChained() throws Exception {

        ReadRequest readRequest = presentReadRequest();
        Flowable<NotifResponse> notifFlow = client
                .dataWindowNotifications(readRequest, Data.class);
        TestSubscriber<NotifResponse> testSubscriber = TestSubscriber.create();
        notifFlow.observeOn(Schedulers.io())
                .filter(n -> n.getDataItem() instanceof NextWindow)
                .map(NotifResponse::getDataItem)
                .cast(NextWindow.class)
                .flatMap(nw -> client
                        .dataWindowNotifications(nw.getNext(), Data.class))
                .subscribe(testSubscriber);

        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotComplete();
        List<NotifResponse> events = testSubscriber.values();
        Assert.assertEquals(3, events.size());
        Optional<NextWindow> maybeNextWindow = nextWindowEvent(events);

        Assert.assertTrue(maybeNextWindow.isPresent());
        ReadRequest nextRead = maybeNextWindow.get().getNext();

        List<DataWindowChangeEvent<Data>> changeEvents = changeEvents(events);
        List<Data> dataList = changeEvents.stream().map(DataWindowChangeEvent::getItem)
                .collect(Collectors.toList());
        Assert.assertTrue(contains(dataList, new Data("2", "2")));
        Assert.assertTrue(contains(dataList, new Data("3", "3")));

        Assert.assertNotNull(nextRead);
        Assert.assertNotNull(nextRead.getWindowStartWith());
    }

    @Test
    public void absentReadNotifications() throws Exception {

        ReadRequest readRequest = missingReadRequest();
        Flowable<NotifResponse> notifFlow = client
                .dataWindowNotifications(readRequest, Data.class);
        TestSubscriber<NotifResponse> testSubscriber = TestSubscriber.create();
        notifFlow.observeOn(Schedulers.io())
                .subscribe(testSubscriber);
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotComplete();
        List<NotifResponse> events = testSubscriber.values();
        Assert.assertEquals(1, events.size());
        DataWindowNotif dataItem = events.get(0).getDataItem();
        Assert.assertTrue(dataItem instanceof NextWindow);
        Assert.assertTrue(((NextWindow) dataItem).getNext().getWindowStartWith().isEmpty());
    }

    private <T> boolean contains(List<T> items, T item) {
        for (T t : items) {
            if (t.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private Optional<NextWindow> nextWindowEvent(List<NotifResponse> notifResponses) {

        for (NotifResponse event : notifResponses) {
            DataWindowNotif dataItem = event.getDataItem();
            if (dataItem instanceof NextWindow) {
                return Optional.of((NextWindow) dataItem);
            }
        }
        return Optional.empty();
    }

    private <T> List<DataWindowChangeEvent<T>> changeEvents(List<NotifResponse> notifResponses) {
        List<DataWindowChangeEvent<T>> res = new ArrayList<>();
        for (NotifResponse event : notifResponses) {
            DataWindowNotif dataItem = event.getDataItem();
            if (dataItem instanceof DataWindowChangeEvent) {
                res.add((DataWindowChangeEvent<T>) dataItem);
            }
        }
        return res;
    }

    private String lastWindowStartKey(TestSubscriber<ReadResponse<Data>> testSubscriber,
                                      int valueCount) {
        return testSubscriber
                .values()
                .get(valueCount - 1)
                .getReadRequest()
                .getWindowStartWith();
    }

    private ReadRequest presentReadRequest() {
        return Requests
                .readRequest("test", "read")
                .asc()
                .windowWithSize(WINDOW_SIZE)
                .orderByKey()
                .build();
    }

    private ReadRequest missingReadRequest() {
        return Requests
                .readRequest("foo", "bar")
                .asc()
                .windowWithSize(WINDOW_SIZE)
                .orderByKey()
                .build();
    }


    private TestSubscriber<ReadResponse<Data>> requestStreamSubscriber() {
        return new TestSubscriber<ReadResponse<Data>>(REQUEST_N) {
            @Override
            public void onNext(ReadResponse<Data> o) {
                super.onNext(o);
                request(REQUEST_N);
            }
        };
    }

    private boolean assertWindowContent(ReadResponse<Data> window, int index) {
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
