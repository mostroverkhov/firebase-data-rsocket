package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.handler.UnknownHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.delete.DeleteHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.DataWindowHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.read.NotifHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.handler.write.WritePushHandler;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.DeleteMapper;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.ReadMapper;
import com.github.mostroverkhov.firebase_rsocket.internal.mapper.WriteMapper;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Op;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
final class Routes {

    public static Router router() {
        return new Router()
                .route(
                        Op.key(), Op.DATA_WINDOW.value(),
                        (k, v) -> new Router.Route()
                                .mapper(new ReadMapper(k, v))
                                .handler(new DataWindowHandler(k, v)))
                .route(
                        Op.key(), Op.DATA_WINDOW_NOTIF.value(),
                        (k, v) -> new Router.Route()
                                .mapper(new ReadMapper(k, v))
                                .handler(new NotifHandler(k, v)))
                .route(
                        Op.key(), Op.WRITE_PUSH.value(),
                        (k, v) -> new Router.Route()
                                .mapper(new WriteMapper(k, v))
                                .handler(new WritePushHandler(k, v)))
                .route(
                        Op.key(), Op.DELETE.value(),
                        (k, v) -> new Router.Route()
                                .mapper(new DeleteMapper(k, v))
                                .handler(new DeleteHandler(k, v)))
                .defaultHandler(UnknownHandler::new);
    }
}
