package com.github.mostroverkhov.firebase_rsocket.model.notifications;


import com.github.mostroverkhov.firebase_rsocket.model.read.ReadRequest;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotifResponse {
    private final ReadRequest nextDataWindow;
    private final NotifEventKind kind;
    private final String item;

    public static NotifResponse nextWindow(ReadRequest nextDataWindow) {
        return new NotifResponse(nextDataWindow);
    }

    public static NotifResponse changeEvent(NotifEventKind kind, String item) {
        return new NotifResponse(kind, item);
    }

    NotifResponse(ReadRequest nextDataWindow) {
        Objects.requireNonNull(nextDataWindow, "nextDataWindow");
        this.nextDataWindow = nextDataWindow;
        this.kind = null;
        this.item = null;
    }

    NotifResponse(NotifEventKind kind, String item) {
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
