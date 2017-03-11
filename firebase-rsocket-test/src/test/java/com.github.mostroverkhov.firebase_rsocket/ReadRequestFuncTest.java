package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class ReadRequestFuncTest extends AbstractTest{

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
