package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class LogConfig {
    private final Logger logger;
    private final Deployment deployment;

   public LogConfig(Logger logger, Deployment deployment) {
        this.logger = logger;
        this.deployment = deployment;
    }

    public Logger getLogger() {
        return logger;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public static class LogFormatter {
        private final String version;
        private final String host;
        private final int port;

        public LogFormatter(String version, String host, int port) {
            this.version = version;
            this.host = host;
            this.port = port;
        }

        public Logger.Log.Row requestRow(String uuid, Operation request) {
            return new Logger.Log.Row("request", uuid,
                    request,
                    System.currentTimeMillis(),
                    version,
                    host,
                    port);
        }

        public Logger.Log.Row responseRow(String uuid, Object response) {
            return new Logger.Log.Row("response", uuid,
                    response,
                    System.currentTimeMillis(),
                    version,
                    host,
                    port);

        }

        public Logger.Log.Row responseErrorRow(String uuid, Throwable error) {
            return new Logger.Log.Row("response_error", uuid,
                    errorMsg(error),
                    System.currentTimeMillis(),
                    version,
                    host,
                    port);

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
    }

    public static class Deployment {

        private final String host;
        private final int port;
        private final String version;

        public Deployment(String host, int port, String version) {
            this.host = host;
            this.port = port;
            this.version = version;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getVersion() {
            return version;
        }
    }
}
