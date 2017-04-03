package com.github.mostroverkhov.firebase_rsocket;

import io.reactivex.Flowable;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public interface Logger {

    void log(Log.Row row);

    Log log();

    interface Log {

        default Flowable<Row> logFlow() {
            return Flowable.empty();
        }

        class Row {
            private final String type;
            private final String uuid;
            private final Object payload;
            private final long timeStamp;
            private final String version;
            private final String host;
            private final int port;

            public Row(String type, String uuid,
                       Object payload,
                       long timeStamp,
                       String version,
                       String host,
                       int port) {
                this.type = type;
                this.payload = payload;
                this.uuid = uuid;
                this.timeStamp = timeStamp;
                this.version = version;
                this.host = host;
                this.port = port;
            }

            public String getType() {
                return type;
            }

            public String getUuid() {
                return uuid;
            }

            public Object payload() {
                return payload;
            }


            public long timeStamp() {
                return timeStamp;
            }

            public String version() {
                return version;
            }

            public String host() {
                return host;
            }

            public int port() {
                return port;
            }
        }
    }
}
