package com.github.mostroverkhov.firebase_rsocket.internal.logging;

import com.github.mostroverkhov.firebase_rsocket.LogConfig;
import com.github.mostroverkhov.firebase_rsocket.Logger;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

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

    public Optional<Logger.Log.Row> logError(Throwable err) {
        return log(uid, logging, (lf, uid) -> lf.responseErrorRow(uid.toString(), err));
    }

    public Optional<Logger.Log.Row> logResponse(Object response) {
        return log(uid, logging, (formatter, uidV) -> formatter.responseRow(uidV.toString(), response));
    }

    public Operation logRequest(Operation op) {
        log(uid, logging, (formatter, uidV) -> formatter.requestRow(uidV.toString(), op));
        return op;
    }

    private static Optional<Logger.Log.Row> log(Optional<UUID> uid,
                                                Optional<Logging> logging,
                                                BiFunction<LogConfig.LogFormatter, UUID, Logger.Log.Row> mapper) {
        return logging.flatMap(l -> {
            Logger logger = l.getLogConfig().getLogger();
            LogConfig.LogFormatter logFormatter = l.getLogFormatter();
            return uid.map(uidV -> {
                Logger.Log.Row row = mapper.apply(logFormatter, uidV);
                logger.log(row);
                return row;
            });
        });
    }
}
