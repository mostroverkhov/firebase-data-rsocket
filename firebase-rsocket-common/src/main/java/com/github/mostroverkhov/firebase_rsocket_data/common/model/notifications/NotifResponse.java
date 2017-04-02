package com.github.mostroverkhov.firebase_rsocket_data.common.model.notifications;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class NotifResponse {

    private final DataWindowNotif dataItem;

    public NotifResponse(DataWindowNotif dataItem) {
        this.dataItem = dataItem;
    }

    public DataWindowNotif getDataItem() {
        return dataItem;
    }
}
