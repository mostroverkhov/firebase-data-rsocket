package com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;

/**
 * Represents query for next data window
 */
public class NextWindow extends DataWindowNotif {

    private final ReadRequest next;

    public NextWindow(ReadRequest next) {
        super(NotifKind.NEXT_WINDOW);
        this.next = next;
    }

    public ReadRequest getNext() {
        return next;
    }
}
