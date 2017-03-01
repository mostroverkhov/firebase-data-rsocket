package com.github.mostroverkhov.firebase_rsocket_data.common.model;

import java.util.List;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public final class DataWindow<T> {
    private final Query query;
    private final List<T> data;

    public DataWindow(Query query, List<T> data) {
        this.query = query;
        this.data = data;
    }

    public Query getQuery() {
        return query;
    }

    public List<T> getData() {
        return data;
    }
}
