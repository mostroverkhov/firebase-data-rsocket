package com.github.mostroverkhov.firebase_rsocket_data.common.model;

import java.util.List;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public final class DataWindow<T> {
    private final ReadQuery readQuery;
    private final List<T> data;

    public DataWindow(ReadQuery readQuery, List<T> data) {
        this.readQuery = readQuery;
        this.data = data;
    }

    public ReadQuery getReadQuery() {
        return readQuery;
    }

    public List<T> getData() {
        return data;
    }
}
