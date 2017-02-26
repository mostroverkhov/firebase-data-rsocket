package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.handlers.adapters.DefaultHandlerAdapter;
import com.github.mostroverkhov.firebase_rsocket.handlers.adapters.RequestHandlerAdapter;
import com.github.mostroverkhov.firebase_rsocket.handlers.requesthandlers.*;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.google.gson.Gson;
import io.reactivesocket.AbstractReactiveSocket;
import io.reactivesocket.ConnectionSetupPayload;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.lease.DisabledLeaseAcceptingSocket;
import io.reactivesocket.lease.LeaseEnforcingSocket;
import io.reactivesocket.server.ReactiveSocketServer;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerSocketAcceptor implements ReactiveSocketServer.SocketAcceptor {

    private Authenticator authenticator;
    private HandlerManager handlerManager;
    private final Gson gson;

    ServerSocketAcceptor(Authenticator authenticator,
                         HandlerManager handlerManager,
                         Gson gson) {
        this.authenticator = authenticator;
        this.handlerManager = handlerManager;
        this.gson = gson;
    }

    @Override
    public LeaseEnforcingSocket accept(ConnectionSetupPayload setupPayload,
                                       ReactiveSocket reactiveSocket) {
        return new DisabledLeaseAcceptingSocket(
                new FirebaseReactiveSocket(
                        new SocketContext(authenticator, gson),
                        handlerManager,
                        new DefaultHandlerAdapter(gson)));
    }

    private static class FirebaseReactiveSocket extends AbstractReactiveSocket {

        private final SocketContext context;
        private final HandlerManager handlerManager;
        private final RequestHandlerAdapter<?> requestHandlerAdapter;

        public FirebaseReactiveSocket(SocketContext context,
                                      HandlerManager handlerManager,
                                      RequestHandlerAdapter<?> requestHandlerAdapter) {
            this.context = context;
            this.handlerManager = handlerManager;
            this.requestHandlerAdapter = requestHandlerAdapter;
        }

        @Override
        public Publisher<Payload> requestStream(Payload payload) {
            String request = string(payload);
            return requestHandlerAdapter
                    .adapt(request)
                    .map(this::handleRequest)
                    .orElseGet(() -> Flowable.error(requestMissingHandlerAdapter(request)));
        }

        private Publisher<Payload> handleRequest(Operation adaptedRequest) {
            return context.authenticator().authenticate()
                    .andThen(Flowable.defer(
                            () -> handlerManager
                                    .handler(adaptedRequest)
                                    .handle(context, adaptedRequest)));
        }

        private static String string(Payload payload) {
            ByteBuffer bb = payload.getData();
            byte[] b = new byte[bb.remaining()];
            bb.get(b);
            try {
                return new String(b, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("String encoding error", e);
            }
        }
    }

    private static FirebaseRsocketMessageFormatException
    requestMissingHandlerAdapter(String request) {
        return new FirebaseRsocketMessageFormatException(
                "No handler adapter for request: " + request);
    }

    public static class SocketContext {
        private Authenticator authenticator;
        private final Gson gson;

        public SocketContext(Authenticator authenticator, Gson gson) {
            this.authenticator = authenticator;
            this.gson = gson;
        }

        public Authenticator authenticator() {
            return authenticator;
        }

        public Gson gson() {
            return gson;
        }
    }

}
