package com.github.mostroverkhov.firebase_rsocket.internal.auth.sources;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsFactory;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.NonResolvedCredentials;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.TaggedStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class PropCredentialsFactory implements CredentialsFactory {

    @Override
    public NonResolvedCredentials credentials(TaggedStream credsRef) {
        Properties props = new Properties();
        InputStream credsStream = credsRef.getStream().get();
        if (credsStream == null) {
            throw new IllegalArgumentException("Error while reading credentials: " + credsRef.getDesc());
        }

        try {
            props.load(credsStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while loading property file: " + credsRef.getDesc(), e);
        }
        String serviceFileRef = props.getProperty("authFile");
        String dbUrl = props.getProperty("dbUrl");
        String dbUserId = props.getProperty("dbUserId");

        String invalidMsg = validate(credsRef.getDesc(),
                new Prop(serviceFileRef, "authFile"),
                new Prop(dbUrl, "dbUrl"),
                new Prop(dbUserId, "dbUserId"));
        if (invalidMsg.isEmpty()) {
            return new NonResolvedCredentials(dbUrl, dbUserId, serviceFileRef);
        } else {
            throw new IllegalArgumentException(invalidMsg);
        }
    }

    static String validate(String propFile, Prop... props) {
        List<String> emptyProps = new ArrayList<>();
        for (Prop prop : props) {
            if (isEmpty(prop.getValue())) {
                emptyProps.add(prop.getName());
            }
        }
        return buildErrorMsg(propFile, emptyProps);
    }

    static String buildErrorMsg(String propFile, List<String> emptyProps) {
        if (emptyProps.isEmpty()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Property file ").append(propFile).append(" lacks required properties: ");

            for (String emptyProp : emptyProps) {
                sb.append(emptyProp).append(", ");
            }
            return sb.toString();
        }
    }

    static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private static class Prop {
        private String value;
        private String name;

        public Prop(String value, String name) {
            this.value = value;
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

}
