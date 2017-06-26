package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class ConfigurationReader {

    private static final String CONFIG = "--config";
    private static final String CONFIGNAME = "config";
    private static final String PORT = "--port";
    private static final String PORTNAME = "port";

    public Configuration read(String[] args) throws ArgsException {
        System.out.println("Args: " + Arrays.toString(args));
        ArgumentParser parser = createParser();
        try {
            Namespace namespace = parser.parseArgs(args);
            String portStr = namespace.get(PORTNAME);
            String config = namespace.get(CONFIGNAME);
            System.out.println("Ns: " + String.valueOf(namespace));
            try {
                Integer port = Integer.parseInt(portStr);
                return new Configuration(port, config);
            } catch (NumberFormatException e) {
                throw new ArgsException("Cant parse port as Integer: " + portStr);
            }
        } catch (ArgumentParserException e) {
            throw new ArgsException(String.format("Arguments parsing error: %s \n Args format is: \n %s",
                    Arrays.toString(args),
                    e.getParser().formatHelp()),
                    e);
        }
    }

    private static ArgumentParser createParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("firebase-rsocket-server");
        parser.addArgument(CONFIG)
                .required(true)
                .dest(CONFIGNAME)
                .help("server config file");
        parser.addArgument(PORT)
                .required(true)
                .dest(PORTNAME)
                .help("server port");

        return parser;
    }
}
