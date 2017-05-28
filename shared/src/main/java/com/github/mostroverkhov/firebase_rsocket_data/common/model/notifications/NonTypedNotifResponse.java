package com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NonTypedNotifResponse {
    private final ReadRequest nextDataWindow;
    private final NotifEventKind kind;
    private final String item;

    public static NonTypedNotifResponse nextWindow(ReadRequest nextDataWindow) {
        return new NonTypedNotifResponse(nextDataWindow);
    }

    public static NonTypedNotifResponse changeEvent(NotifEventKind kind, String item) {
        return new NonTypedNotifResponse(kind, item);
    }

    NonTypedNotifResponse(ReadRequest nextDataWindow) {
        Objects.requireNonNull(nextDataWindow, "nextDataWindow");
        this.nextDataWindow = nextDataWindow;
        this.kind = null;
        this.item = null;
    }

    NonTypedNotifResponse(NotifEventKind kind, String item) {
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

    public String getItem() {
        return item;
    }


}
