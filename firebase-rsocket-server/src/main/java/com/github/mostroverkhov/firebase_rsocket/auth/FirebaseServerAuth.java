package com.github.mostroverkhov.firebase_rsocket.auth;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Maksym Ostroverkhov on 28.02.2017.
 */

public class FirebaseServerAuth {

    private static volatile FirebaseServerAuth instance;

    private final String serviceAccountFileName;
    private final String databaseUrl;
    private final String uid;
    private final AtomicBoolean appInitSignal = new AtomicBoolean();

    private FirebaseServerAuth(String serviceAccountFileName,
                              String databaseUrl,
                              String uid) {
        this.serviceAccountFileName = serviceAccountFileName;
        this.databaseUrl = databaseUrl;
        this.uid = uid;
    }

    public static FirebaseServerAuth getInstance(String serviceAccountFileName,
                                                 String databaseUrl,
                                                 String uid) {
        if (instance == null) {
            synchronized (FirebaseServerAuth.class) {
                if (instance == null) {
                    instance = new FirebaseServerAuth(serviceAccountFileName, databaseUrl, uid);
                }
            }
        }
        return instance;
    }

    public Completable authenticate() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                if (!e.isDisposed()) {
                    if (FirebaseApp.getApps().isEmpty()) {
                        InputStream stream = getClass().getClassLoader()
                                .getResourceAsStream(serviceAccountFileName);
                        if (stream != null) {
                            Map<String, Object> auth = new HashMap<>();
                            auth.put("uid", uid);
                            FirebaseOptions options = new FirebaseOptions.Builder()
                                    .setServiceAccount(stream)
                                    .setDatabaseUrl(databaseUrl)
                                    .setDatabaseAuthVariableOverride(auth)
                                    .build();
                            if (appInitSignal.compareAndSet(false, true)) {
                                FirebaseApp.initializeApp(options);
                            }
                            if (!e.isDisposed()) {
                                e.onComplete();
                            }
                        } else if (!e.isDisposed()) {
                            e.onError(new IllegalStateException("Error while reading service " +
                                    "account file: "
                                    + serviceAccountFileName));
                        }
                    } else {
                        e.onComplete();
                    }
                }
            }
        });
    }
}