package com.github.mostroverkhov.firebase_rsocket.internal.mapper.gson;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.google.gson.Gson;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytesToReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteClientMapper extends BaseClientMapper<DeleteRequest, DeleteResponse> {

    public DeleteClientMapper() {
    }

    @Override
    public Publisher<DeleteResponse> map(byte[] response) {
        return Flowable.fromCallable(() -> mapDelete(
                gson(),
                response))
                .onErrorResumeNext(mappingError("Error while mapping Delete response"));
    }

    private static DeleteResponse mapDelete(Gson gson,
                                            byte[] payload) {
        return gson.fromJson(bytesToReader(payload), DeleteResponse.class);
    }
}
