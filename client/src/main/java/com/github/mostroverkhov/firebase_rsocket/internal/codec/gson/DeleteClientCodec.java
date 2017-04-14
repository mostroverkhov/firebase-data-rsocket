package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.delete.DeleteResponse;
import com.google.gson.Gson;

import java.nio.charset.Charset;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytesToReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DeleteClientCodec extends GsonClientCodec<DeleteRequest, DeleteResponse> {

    public DeleteClientCodec() {
    }

    @Override
    public DeleteResponse map(byte[] response) {
        return mapDelete(serializer(), response);
    }

    private static DeleteResponse mapDelete(GsonSerializer gsonSerializer,
                                            byte[] payload) {
        Gson gson = gsonSerializer.getGson();
        Charset charset = Charset.forName(gsonSerializer.getEncoding());
        return gson.fromJson(
                bytesToReader(payload, charset),
                DeleteResponse.class);
    }
}
