package com.github.mostroverkhov.firebase_rsocket_data.common.model.read;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;

/**
 * Created by Maksym Ostroverkhov on 27.02.17.
 */
public class ReadRequest {
    private final Path path;
    private final int windowSize;
    private final OrderDir orderDir;
    private final OrderBy orderBy;
    private final String orderByChildKey;
    private final String windowStartWith;

    public ReadRequest(Path path,
                       int windowSize,
                       OrderDir orderDir,
                       OrderBy orderBy,
                       String orderByChildKey,
                       String windowStartWith) {
        this.path = path;
        this.windowSize = windowSize;
        this.orderDir = orderDir;
        this.orderBy = orderBy;
        this.orderByChildKey = orderByChildKey;
        this.windowStartWith = windowStartWith;
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
                ", path=" + path +
                ", windowSize=" + windowSize +
                ", orderDir=" + orderDir +
                ", orderBy=" + orderBy +
                ", orderByChildKey='" + orderByChildKey + '\'' +
                ", windowStartWith='" + windowStartWith + '\'' +
                '}';
    }
}
