package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_data_rxjava.rx.FirebaseDatabaseManager;
import com.github.mostroverkhov.firebase_data_rxjava.rx.model.WriteResult;
import com.github.mostroverkhov.firebase_rsocket.auth.PropsCredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.auth.ServerAuthenticator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Completable;
import rx.Observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class DataFixture {

    public static final int ITEM_COUNT = 10;
    public static final String[] TEST_READ_PATH = {"test", "read"};
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

    public Completable fillSampleData() {

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
        Completable errorSignal = Completable.timer(timeOutSec, TimeUnit.SECONDS)
                .andThen(Completable.error(
                        new TimeoutException("Fixture Db write timeout")));
        Completable writeCompleteSignal = RxJavaInterop.toV2Completable(
                allWritesStream.doOnNext(System.out::println).take(itemCount)
                        .toCompletable());

        return writeCompleteSignal.ambWith(errorSignal);
    }

    public static void main(String... args) {

        DataFixture dataFixture = new DataFixture(
                ITEM_COUNT,
                () -> path(FirebaseDatabase.getInstance().getReference(), TEST_READ_PATH),
                WRITE_TIMEOUT_SEC);

        ServerAuthenticator testAuthenticator = new TestAutheticator();
        testAuthenticator.authenticate().toFlowable().flatMap(
                __ -> dataFixture.fillSampleData().toFlowable()).blockingSubscribe();
    }

    private static class TestAutheticator extends ServerAuthenticator {
        public TestAutheticator() {
            super(new PropsCredentialsFactory("creds.properties"));
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