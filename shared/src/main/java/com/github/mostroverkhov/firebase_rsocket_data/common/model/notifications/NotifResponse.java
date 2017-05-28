package com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotifResponse<T> {
    private final ReadRequest nextDataWindow;
    private final NotifEventKind kind;
    private final T item;

    public static <T> NotifResponse<T> nextWindow(ReadRequest nextDataWindow) {
        return new NotifResponse<>(nextDataWindow);
    }

    public static <T> NotifResponse<T> changeEvent(NotifEventKind kind, T item) {
        return new NotifResponse<>(kind, item);
    }

    NotifResponse(ReadRequest nextDataWindow) {
        Objects.requireNonNull(nextDataWindow, "nextDataWindow");
        this.nextDataWindow = nextDataWindow;
        this.kind = null;
        this.item = null;
    }

     NotifResponse(NotifEventKind kind, T item) {
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

    public NotifEventKind getKind() {
        return kind;
    }

    public T getItem() {
        return item;
    }

}

