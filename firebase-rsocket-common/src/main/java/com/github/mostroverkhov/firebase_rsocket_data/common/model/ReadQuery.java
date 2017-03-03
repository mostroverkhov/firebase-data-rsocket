package com.github.mostroverkhov.firebase_rsocket_data.common.model;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class ReadQuery {
    private volatile String operation;
    private final Path path;
    private final int windowSize;
    private final OrderDir orderDir;
    private final OrderBy orderBy;
    private final String orderByChildKey;
    private final String windowStartWith;

    public ReadQuery(String operation,
                     Path path,
                     int windowSize,
                     OrderDir orderDir,
                     OrderBy orderBy,
                     String orderByChildKey,
                     String windowStartWith) {
        this.operation = operation;
        this.path = path;
        this.windowSize = windowSize;
        this.orderDir = orderDir;
        this.orderBy = orderBy;
        this.orderByChildKey = orderByChildKey;
        this.windowStartWith = windowStartWith;
    }

    public ReadQuery setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    public Path getPath() {
        return path;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public OrderDir getOrderDir() {
        return orderDir;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public boolean isAsc() {
        return orderDir == OrderDir.ASC;
    }

    public boolean isDesc() {
        return !isAsc();
    }

    public String getOrderByChildKey() {
        return orderByChildKey;
    }

    public String getWindowStartWith() {
        return windowStartWith;
    }

    public enum OrderBy {
        CHILD, KEY, VALUE
    }

    public enum OrderDir {
        ASC, DESC
    }

    @Override
    public String toString() {
        return "Query{" +
                "operation='" + operation + '\'' +
                ", path=" + path +
                ", windowSize=" + windowSize +
                ", orderDir=" + orderDir +
                ", orderBy=" + orderBy +
                ", orderByChildKey='" + orderByChildKey + '\'' +
                ", windowStartWith='" + windowStartWith + '\'' +
                '}';
    }
}
