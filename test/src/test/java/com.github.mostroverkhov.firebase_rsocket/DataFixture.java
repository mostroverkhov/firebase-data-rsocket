package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsAuthenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.sources.ClasspathPropsCredentialsSource;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import rx.Observable;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class DataFixture {

    private static final int ITEM_COUNT = 10;
    private static final String[] TEST_READ_PATH = {"test", "read"};
    private static final int WRITE_TIMEOUT_SEC = 10;

    private final int itemCount;
    private Supplier<DatabaseReference> databaseReference;
    private final int timeOutSec;

    public DataFixture(int itemCount,
                       Supplier<DatabaseReference> databaseReference,
                       int timeOutSec) {
        this.itemCount = itemCount;
        this.databaseReference = databaseReference;
        this.timeOutSec = timeOutSec;
    }

    public Mono<Void> fillSampleData() {
        return Mono.defer(() -> {
            final DatabaseReference test = databaseReference.get();
            FirebaseDatabaseManager manager =
                new FirebaseDatabaseManager(test);

            Observable<WriteResult> allWritesStream = Observable.empty();
            for (int i = 0; i < itemCount; i++) {
                DatabaseReference newItem = test.push();
                Observable<WriteResult> writeStream = manager.data(__ -> newItem).setValue(
                    new Data(String.valueOf(i), String.valueOf(i)));
                allWritesStream = allWritesStream.mergeWith(writeStream);
            }
            Duration timeout = Duration.ofSeconds(timeOutSec);
            Mono<Void> timeoutError = Mono.delay(timeout)
                .then(Mono.error(
                    new TimeoutException("Fixture Db write timeout")));

            Mono<Void> writeComplete = RxJava2Adapter.completableToMono(
                RxJavaInterop.toV2Completable(
                    allWritesStream
                        .doOnNext(System.out::println)
                        .take(itemCount)
                        .toCompletable()));

            return Mono.first(writeComplete, timeoutError);
        });
    }

    public static void main(String... args) {

        DataFixture dataFixture = new DataFixture(
                ITEM_COUNT,
                () -> path(FirebaseDatabase.getInstance().getReference(), TEST_READ_PATH),
                WRITE_TIMEOUT_SEC);

        CredentialsAuthenticator testAuthenticator = new TestAutheticator();
        testAuthenticator.authenticate().then(
                dataFixture.fillSampleData()).block();
    }

    private static class TestAutheticator extends CredentialsAuthenticator {
        public TestAutheticator() {
            super(new ClasspathPropsCredentialsSource("creds.properties"));
        }
    }


    private static DatabaseReference path(DatabaseReference root,
                                          String[] children) {
        for (String child : children) {
            root = root.child(child);
        }
        return root;
    }
}