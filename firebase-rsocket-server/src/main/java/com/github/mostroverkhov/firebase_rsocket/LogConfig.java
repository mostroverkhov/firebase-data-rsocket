package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class LogConfig {
    private final Logger logger;

    public LogConfig(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public static class LogFormatter {

        public LogFormatter() {
        }

        public Logger.Row requestRow(String uuid, Operation request) {
            return new Logger.Row(
                    Kind.REQUEST.code(),
                    uuid,
                    request,
                    System.currentTimeMillis());
        }

        public Logger.Row responseRow(String uuid, Object response) {
            return new Logger.Row(
                    Kind.RESPONSE_SUCCESS.code(),
                    uuid,
                    response,
                    System.currentTimeMillis());

        }

        public Logger.Row responseErrorRow(String uuid, Throwable error) {
            return new Logger.Row(
                    Kind.RESPONSE_ERROR.code(),
                    uuid,
                    errorMsg(error),
                    System.currentTimeMillis());

        }

        private String errorMsg(Throwable err) {
            String name = err.getClass().getName();
            Throwable cause = err.getCause();
            String msg = err.getMessage();
            StringBuilder sb = new StringBuilder();

            String errMsg = sb
                    .append("Error: ")
                    .append(name).append(": ")
                    .append(msg).append(": ")
                    .append(cause).toString();

            return errMsg;
        }

        private enum Kind {
            REQUEST("request"),
            RESPONSE_SUCCESS("response"),
            RESPONSE_ERROR("response_error");

            private final String code;

            Kind(String code) {
                this.code = code;
            }

            public String code() {
                return code;
            }
        }
    }
}
