package com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DataWindowChangeEvent<T> extends DataWindowNotif {
    private final EventKind kind;
    private final T item;

    public DataWindowChangeEvent(T item,
                                 EventKind kind) {
        super(NotifKind.EVENT);
        assertArgs(item, kind);
        this.item = item;
        this.kind = kind;
    }

    public T getItem() {
        return item;
    }

    public EventKind getKind() {
        return kind;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataWindowChangeEvent{");
        sb.append(", kind=").append(kind);
        sb.append(", item=").append(item);
        sb.append('}');
        return sb.toString();
    }


    public enum EventKind {
        ADDED,
        CHANGED,
        MOVED,
        REMOVED;
    }

    private void assertArgs(T item, EventKind kind) {
        if (item == null || kind == null) {
            throw new IllegalArgumentException("Item and Kind should not be null");
        }
    }

}
