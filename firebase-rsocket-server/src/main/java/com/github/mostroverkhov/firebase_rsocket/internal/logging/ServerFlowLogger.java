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
    private final Optional<UUID> uid;
    private final Optional<Logging> logging;

    public ServerFlowLogger(Optional<UUID> uid,
                            Optional<Logging> logging) {
        this.uid = uid;
        this.logging = logging;
    }

    public Optional<Logger.Row> logError(Throwable err) {
        return log(uid, logging, (lf, uid) -> lf.responseErrorRow(uid.toString(), err));
    }

    public Optional<Logger.Row> logResponse(Object response) {
        return log(uid, logging, (formatter, uidV) -> formatter.responseRow(uidV.toString(), response));
    }

    public Object logRequest(Object data) {
        log(uid, logging, (formatter, uidV) -> formatter.requestRow(uidV.toString(), data));
        return data;
    }

    private static Optional<Logger.Row> log(Optional<UUID> uid,
                                            Optional<Logging> logging,
                                            BiFunction<LogFormatter, UUID, Logger.Row> mapper) {
        return logging.flatMap(l -> {
            Logger logger = l.getLogger();
            LogFormatter logFormatter = l.getLogFormatter();
            return uid.map(uidV -> {
                Logger.Row row = mapper.apply(logFormatter, uidV);
                logger.log(row);
                return row;
            });
        });
    }
}
