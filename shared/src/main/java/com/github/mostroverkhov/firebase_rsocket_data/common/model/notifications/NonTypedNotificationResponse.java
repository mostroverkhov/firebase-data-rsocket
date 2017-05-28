package com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NonTypedNotificationResponse {
    private final ReadRequest nextDataWindow;
    private final EventKind kind;
    private final String item;

    public static NonTypedNotificationResponse nextWindow(ReadRequest nextDataWindow) {
        return new NonTypedNotificationResponse(nextDataWindow);
    }

    public static NonTypedNotificationResponse changeEvent(EventKind kind, String item) {
        return new NonTypedNotificationResponse(kind, item);
    }

    NonTypedNotificationResponse(ReadRequest nextDataWindow) {
        Objects.requireNonNull(nextDataWindow, "nextDataWindow");
        this.nextDataWindow = nextDataWindow;
        this.kind = null;
        this.item = null;
    }

    NonTypedNotificationResponse(EventKind kind, String item) {
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

    public String getItem() {
        return item;
    }


}
