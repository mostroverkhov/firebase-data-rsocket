package com.github.mostroverkhov.firebase_rsocket.server.tcp.entrypoint;

import java.util.Arrays;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
class ConfigurationReader {

  private static final String HOST = "--host";
  private static final String HOSTNAME = "host";
  private static final String PORT = "--port";
  private static final String PORTNAME = "port";
  private static final String CONFIG = "--config";
  private static final String CONFIGNAME = "config";

  public Configuration read(String[] args) throws ArgsException {
    ArgumentParser parser = argsParser();
    try {
      Namespace namespace = parser.parseArgs(args);
      String host = namespace.get(HOSTNAME);
      String portStr = namespace.get(PORTNAME);
      String config = namespace.get(CONFIGNAME);
      try {
        Integer port = Integer.parseInt(portStr);
        return new Configuration(host, port, config);
      } catch (NumberFormatException e) {
        throw new ArgsException("Cant parse port as Integer: " + portStr);
      }
    } catch (ArgumentParserException e) {
      throw new ArgsException(
          String.format(
              "Arguments parsing error: %s \n Args format is: \n %s",
              Arrays.toString(args), e.getParser().formatHelp()),
          e);
    }
  }

  private static ArgumentParser argsParser() {
    ArgumentParser parser = ArgumentParsers.newArgumentParser("firebase-rsocket-server");
    parser.addArgument(HOST).required(true).dest(HOSTNAME).help("server bind address");
    parser.addArgument(PORT).required(true).dest(PORTNAME).help("server port");
    parser.addArgument(CONFIG).required(true).dest(CONFIGNAME).help("server config file");
    return parser;
  }
}
