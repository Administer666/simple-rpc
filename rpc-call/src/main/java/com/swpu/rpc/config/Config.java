package com.swpu.rpc.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    static Properties properties = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String getServerHost() {
        String value = properties.getProperty("server.host");
        if (value == null) {
            return "localhost";
        } else {
            return value;
        }
    }

    public static int getServerPort() {
        String value = properties.getProperty("server.port");
        if (value == null) {
            return 8080;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static String getSerializer() {
        return properties.getProperty("serializer.algorithm");
    }
}