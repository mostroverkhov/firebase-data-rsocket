package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ResponseMappingException;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.CustomGsonClientCodec;
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
class ClientChain {
    private final ClientConfig clientConfig;
    private final Flowable<ReactiveSocket> rsocket;

    public ClientChain(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.rsocket = rsocket();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public <Req, Resp> Flowable<Resp> request(
            CustomGsonClientCodec<Req, Resp> clientMapper,
            Req request,
            KeyValue metadata) {

        clientMapper.setSerializer(clientConfig.serializer());

        Flowable<Resp> readResponseFlow = rsocket
                .observeOn(Schedulers.io())
                .flatMap(socket -> {
                    BytePayload bytePayload = clientMapper.marshall(metadata, request);
                    Payload requestPayload = Conversions
                            .bytesToPayload(
                                    bytePayload.getData(),
                                    bytePayload.getMetaData());
                    return socket.requestStream(requestPayload);
                })
                .filter(requestStreamDataFrames())
                .map(response -> {
                    Resp responseFlow = clientMapper
                            .map(Conversions.dataToBytes(response));
                    return responseFlow;
                });

        Flowable<Resp> readResponseOrErrorFlow = readResponseFlow
                .onErrorResumeNext(mappingError("Error while processing request " + request));

        return readResponseOrErrorFlow;
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
