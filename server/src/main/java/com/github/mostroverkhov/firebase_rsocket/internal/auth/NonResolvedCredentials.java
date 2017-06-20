package com.github.mostroverkhov.firebase_rsocket.internal.auth;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NonResolvedCredentials {

    private final String dbUrl;
    private final String userId;
    private final String serviceFile;

    public NonResolvedCredentials(String dbUrl, String userId, String serviceFile) {
        this.dbUrl = dbUrl;
        this.userId = userId;
        this.serviceFile = serviceFile;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getServiceFile() {
        return serviceFile;
    }
}
