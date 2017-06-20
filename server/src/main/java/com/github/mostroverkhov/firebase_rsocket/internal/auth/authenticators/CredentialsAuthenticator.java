package com.github.mostroverkhov.firebase_rsocket.internal.auth.authenticators;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.Credentials;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsSource;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.reactivex.Completable;
import io.reactivex.Flowable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class CredentialsAuthenticator implements Authenticator {

    private final Flowable<Credentials> credsFlow;

    public CredentialsAuthenticator(CredentialsSource credsFlow) {
        this.credsFlow = credsFlow.getCreds().toFlowable();
    }

    @Override
    public Completable authenticate() {
        return credsFlow
                .map(credentials -> FirebaseServerAuth.getInstance(
                        credentials.getServiceFile(),
                        credentials.getDbUrl(),
                        credentials.getUserId()))
                .flatMapCompletable(FirebaseServerAuth::authenticate);
    }

    static class FirebaseServerAuth {

        private static volatile FirebaseServerAuth instance;

        private final Supplier<InputStream> serviceFileStream;
        private final String databaseUrl;
        private final String uid;
        private final AtomicBoolean appInitSignal = new AtomicBoolean();

        private FirebaseServerAuth(Supplier<InputStream> serviceFileStream,
                                   String databaseUrl,
                                   String uid) {
            this.serviceFileStream = serviceFileStream;
            this.databaseUrl = databaseUrl;
            this.uid = uid;
        }

        static FirebaseServerAuth getInstance(Supplier<InputStream> serviceFile,
                                              String databaseUrl,
                                              String uid) {
            if (instance == null) {
                synchronized (FirebaseServerAuth.class) {
                    if (instance == null) {
                        instance = new FirebaseServerAuth(serviceFile, databaseUrl, uid);
                    }
                }
            }
            return instance;
        }

        Completable authenticate() {
            return Completable.create(e -> {
                if (!e.isDisposed()) {
                    if (FirebaseApp.getApps().isEmpty()) {
                        Map<String, Object> auth = new HashMap<>();
                        auth.put("uid", uid);
                        FirebaseOptions options = new FirebaseOptions.Builder()
                                .setServiceAccount(serviceFileStream.get())
                                .setDatabaseUrl(databaseUrl)
                                .setDatabaseAuthVariableOverride(auth)
                                .build();
                        if (appInitSignal.compareAndSet(false, true)) {
                            FirebaseApp.initializeApp(options);
                        }
                        if (!e.isDisposed()) {
                            e.onComplete();
                        }
                    } else {
                        e.onComplete();
                    }
                }
            });
        }
    }
}
