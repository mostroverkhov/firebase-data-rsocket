package com.github.mostroverkhov.firebase_rsocket.request;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.google.gson.Gson;
import io.reactivesocket.Payload;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.payloadReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteClientMapper extends BaseClientMapper<DeleteRequest, DeleteResponse> {

    public DeleteClientMapper(Gson gson) {
        super(gson);
    }

    @Override
    public Publisher<DeleteResponse> mapResponse(Payload response) {
        return Flowable.fromCallable(() -> mapDelete(
                gson(),
                response))
                .onErrorResumeNext(mappingError("Error while mapping Delete response"));
    }

    private static DeleteResponse mapDelete(Gson gson,
                                            Payload payload) {
        return gson.fromJson(payloadReader(payload), DeleteResponse.class);
    }
}
