package com.github.mostroverkhov.firebase_rsocket.internal.logging;

import com.github.mostroverkhov.firebase_rsocket.Logger;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class Logging {
    private Logger logger;
    private final LogFormatter logFormatter;

    public Logging(Logger logger,
                   LogFormatter logFormatter) {
        this.logger = logger;
        this.logFormatter = logFormatter;
    }

    public Logger getLogger() {
        return logger;
    }

    public LogFormatter getLogFormatter() {
        return logFormatter;
    }
}
