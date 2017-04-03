package com.github.mostroverkhov.firebase_rsocket.internal.logging;

import com.github.mostroverkhov.firebase_rsocket.LogConfig;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class Logging {
    private final LogConfig logConfig;
    private final LogConfig.LogFormatter logFormatter;

    public Logging(LogConfig logConfig,
                   LogConfig.LogFormatter logFormatter) {
        this.logConfig = logConfig;
        this.logFormatter = logFormatter;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public LogConfig.LogFormatter getLogFormatter() {
        return logFormatter;
    }
}
