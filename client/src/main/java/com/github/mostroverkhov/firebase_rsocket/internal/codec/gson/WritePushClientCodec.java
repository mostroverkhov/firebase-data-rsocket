package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.gson.Gson;

import java.nio.charset.Charset;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytesToReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class WritePushClientCodec<T> extends GsonClientCodec<WriteRequest<T>, WriteResponse> {

    public WritePushClientCodec() {
    }

    @Override
    public WriteResponse map(byte[] response) {
        return mapWrite(serializer(), response);
    }

    private static WriteResponse mapWrite(GsonSerializer gsonSerializer, byte[] payload) {
        Gson gson = gsonSerializer.getGson();
        String encoding = gsonSerializer.getEncoding();
        return gson.fromJson(
                bytesToReader(
                        payload,
                        Charset.forName(encoding)),
                WriteResponse.class);
    }

}
