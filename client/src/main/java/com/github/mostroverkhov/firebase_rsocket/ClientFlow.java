package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.ResponseMappingException;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import io.reactivesocket.Frame;
import io.reactivesocket.FrameType;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.client.ReactiveSocketClient;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

import static io.reactivesocket.client.KeepAliveProvider.never;
import static io.reactivesocket.client.SetupProvider.keepAlive;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
class ClientFlow {
    private final ClientConfig clientConfig;
    private final Flowable<ReactiveSocket> rsocket;

    public ClientFlow(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.rsocket = rsocket();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public <Req, Resp, T> Flowable<T> request(
            ClientCodec<Req, Resp> clientCodec,
            Function<? super Resp, Flowable<T>> transformer,
            Req request,
            KeyValue metadata) {

        Flowable<T> readResponseFlow = rsocket
                .observeOn(Schedulers.io())
                .flatMap(socket -> {
                    BytePayload bytePayload = clientCodec.encode(metadata, request);
                    Payload requestPayload = Conversions
                            .bytesToPayload(
                                    bytePayload.getData(),
                                    bytePayload.getMetaData());
                    return socket.requestStream(requestPayload);
                })
                .filter(requestStreamDataFrames())
                .map(response -> {
                    Resp responseFlow = clientCodec
                            .decode(Conversions.dataToBytes(response));
                    return responseFlow;
                }).flatMap(transformer::apply);

        Flowable<T> readResponseOrErrorFlow = readResponseFlow
                .onErrorResumeNext(mappingError("Error while processing request " + request));

        return readResponseOrErrorFlow;
    }

    public <Req, Resp> Flowable<Resp> request(
            ClientCodec<Req, Resp> clientCodec,
            Req request,
            KeyValue metadata) {
        return request(clientCodec, Flowable::just, request, metadata);
    }


    private Flowable<ReactiveSocket> rsocket() {
        return Flowable.fromPublisher(
                ReactiveSocketClient.create(clientConfig.transport().transportClient(),
                        keepAlive(never()).disableLease())
                        .connect());
    }

    private static <Resp>
    Function<? super Throwable, ? extends Publisher<? extends Resp>>
    mappingError(String msg) {
        return err -> Flowable.error(new ResponseMappingException(msg, err));
    }

    /*workaround for https://github.com/ReactiveSocket/reactivesocket-java/issues/226*/
    private static Predicate<Payload> requestStreamDataFrames() {
        return payload -> (payload instanceof Frame)
                && (((Frame) payload).getType() != FrameType.NEXT_COMPLETE);
    }
}
