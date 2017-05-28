package com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotificationResponse<T> {
    private final ReadRequest nextDataWindow;
    private final EventKind kind;
    private final T item;

    public static <T> NotificationResponse<T> nextWindow(ReadRequest nextDataWindow) {
        return new NotificationResponse<>(nextDataWindow);
    }

    public static <T> NotificationResponse<T> changeEvent(EventKind kind, T item) {
        return new NotificationResponse<>(kind, item);
    }

    NotificationResponse(ReadRequest nextDataWindow) {
        Objects.requireNonNull(nextDataWindow, "nextDataWindow");
        this.nextDataWindow = nextDataWindow;
        this.kind = null;
        this.item = null;
    }

     NotificationResponse(EventKind kind, T item) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(item, "item");
        this.kind = kind;
        this.item = item;
        this.nextDataWindow = null;
    }

    public boolean isNextWindow() {
        return nextDataWindow != null;
    }

    public boolean isChangeEvent() {
        return !isNextWindow();
    }

    public ReadRequest getNextDataWindow() {
        return nextDataWindow;
    }

    public EventKind getKind() {
        return kind;
    }

    public T getItem() {
        return item;
    }

    public enum EventKind {
        ADDED,
        CHANGED,
        MOVED,
        REMOVED;
    }
}

