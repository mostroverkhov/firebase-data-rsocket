package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class WelcomeScreen {
    private static final String WELCOME_MESSAGE = "  ______ _          _                                          _         _                                  \n" +
            " |  ____(_)        | |                                        | |       | |                                 \n" +
            " | |__   _ _ __ ___| |__   __ _ ___  ___   _ __ ___  ___   ___| | __ ___| |_   ___  ___ _ ____   _____ _ __ \n" +
            " |  __| | | '__/ _ \\ '_ \\ / _` / __|/ _ \\ | '__/ __|/ _ \\ / __| |/ // _ \\ __| / __|/ _ \\ '__\\ \\ / / _ \\ '__|\n" +
            " | |    | | | |  __/ |_) | (_| \\__ \\  __/ | |  \\__ \\ (_) | (__|   <|  __/ |_  \\__ \\  __/ |   \\ V /  __/ |   \n" +
            " |_|    |_|_|  \\___|_.__/ \\__,_|___/\\___| |_|  |___/\\___/ \\___|_|\\_\\\\___|\\__| |___/\\___|_|    \\_/ \\___|_|   \n" +
            "                                                                                                          ";
    private final Data data;

    public WelcomeScreen(Data data) {
        this.data = data;
    }

    public void show() {
        System.out.println(WELCOME_MESSAGE);
        System.out.println();
        System.out.println("Version: " + data.getVersion());
        System.out.println("Transport: " + data.getTransport() + " on port: " + data.getPort());
        System.out.println("Started at " + new Date());
    }

    public static class Data {
        private final String version;
        private final String transport;
        private final String port;

        public Data(String version,
                    String transport,
                    String port) {
            this.version = version;
            this.transport = transport;
            this.port = port;
        }

        public String getVersion() {
            return version;
        }

        public String getTransport() {
            return transport;
        }

        public String getPort() {
            return port;
        }
    }
}
