package com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DataWindowNotif {
    private final String notifKind;

    public DataWindowNotif(NotifKind notifKind) {
        this.notifKind = notifKind.name();
    }

    public String getNotifKind() {
        return notifKind;
    }
}

