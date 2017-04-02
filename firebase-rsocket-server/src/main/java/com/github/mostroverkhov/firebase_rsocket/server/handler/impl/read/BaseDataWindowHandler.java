package com.github.mostroverkhov.firebase_rsocket.server.handler.impl.read;

import com.github.mostroverkhov.datawindowsource.model.DataQuery;
import com.github.mostroverkhov.firebase_rsocket.server.handler.impl.BaseRequestHandler;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.google.firebase.database.DatabaseReference;

import static com.github.mostroverkhov.firebase_rsocket.server.handler.impl.HandlerCommon.reference;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public abstract class BaseDataWindowHandler<Resp> extends BaseRequestHandler<ReadRequest, Resp> {

    public BaseDataWindowHandler(Op op) {
        super(op);
    }

    static DataQuery toDataQuery(ReadRequest readRequest) {

        Path path = readRequest.getPath();
        DatabaseReference dataRef = reference(path);

        DataQuery.Builder builder = new DataQuery.Builder(dataRef);
        builder.windowWithSize(readRequest.getWindowSize());
        if (readRequest.isAsc()) {
            builder.asc();
        } else {
            builder.desc();
        }
        String windowStartWith = readRequest.getWindowStartWith();
        if (windowStartWith != null) {
            builder.startWith(windowStartWith);
        }
        ReadRequest.OrderBy orderBy = readRequest.getOrderBy();
        if (orderBy == ReadRequest.OrderBy.KEY) {
            builder.orderByKey();
        } else if (orderBy == ReadRequest.OrderBy.VALUE) {
            builder.orderByValue();
        } else if (orderBy == ReadRequest.OrderBy.CHILD
                && readRequest.getOrderByChildKey() != null) {
            builder.orderByChild(readRequest.getOrderByChildKey());
        } else throw new IllegalStateException("Wrong order by: " + readRequest);

        return builder.build();
    }

    static ReadRequest nextReadRequest(ReadRequest readRequest,
                                       DataQuery dataQuery) {
        return new ReadRequest(readRequest.getOp(),
                readRequest.getPath(),
                readRequest.getWindowSize(),
                readRequest.getOrderDir(),
                readRequest.getOrderBy(),
                readRequest.getOrderByChildKey(),
                dataQuery.getWindowStartWith()
        );
    }
}
