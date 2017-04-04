package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

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
public class WritePushClientMapper<T> extends BaseClientMapper<WriteRequest<T>, WriteResponse> {

    public WritePushClientMapper(Gson gson) {
        super(gson);
    }

    @Override
    public Publisher<WriteResponse> mapResponse(Payload response) {
        return Flowable.fromCallable(() -> mapWrite(
                gson(),
                response))
                .onErrorResumeNext(mappingError("Error while mapping Write response"));
    }

    private static WriteResponse mapWrite(Gson gson, Payload payload) {
        return gson.fromJson(payloadReader(payload), WriteResponse.class);
    }
}
