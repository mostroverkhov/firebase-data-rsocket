package com.github.mostroverkhov.firebase_rsocket.internal.logging;

import com.github.mostroverkhov.firebase_rsocket.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerFlowLogger {
    private final Optional<Logging> logging;

    public ServerFlowLogger(Optional<Logging> logging) {
        this.logging = logging;
    }

    public Throwable logError(Throwable err) {
        log(logging, (formatter, uid) -> formatter.responseErrorRow(uid, err));
        return err;
    }

    public Object logResponse(Object response) {
        log(logging, (formatter, uid) -> formatter.responseRow(uid, response));
        return response;
    }

    public <T> T logRequest(T data) {
        log(logging, (formatter, uid) -> formatter.requestRow(uid, data));
        return data;
    }

    private static Optional<Logger.Row> log(Optional<Logging> logging,
                                            BiFunction<LogFormatter, String, Logger.Row> mapper) {
        return logging.map(l -> {
            Logger logger = l.getLogger();
            String uid = UUID.randomUUID().toString();
            LogFormatter logFormatter = l.getLogFormatter();
            Logger.Row row = mapper.apply(logFormatter, uid);
            logger.log(row);
            return row;
        });
    }
}
