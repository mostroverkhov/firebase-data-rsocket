package com.github.mostroverkhov.firebase_rsocket.request;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.gson.Gson;
import io.reactivesocket.Payload;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.payloadReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WritePushMarshallMap<T> extends BaseMarshallMap<WriteRequest<T>, WriteResponse> {

    public WritePushMarshallMap(Gson gson) {
        super(gson);
    }

    @Override
    public Publisher<WriteResponse> mapResponse(Payload response) {
        WriteResponse writeResponse = mapWrite(
                gson(),
                response);
        return Flowable.just(writeResponse)
                .onErrorResumeNext(mappingError("Error while mapping Write response"));
    }

    private static WriteResponse mapWrite(Gson gson, Payload payload) {
        return gson.fromJson(payloadReader(payload), WriteResponse.class);
    }
}
