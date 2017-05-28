package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.Serializer;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.write.WriteResponse;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytesToReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class GsonClientCodec<Req, Resp> implements ClientCodec<Req, Resp> {

    private GsonSerializer gsonSerializer;
    private Class<Resp> respType;

    public GsonClientCodec(Class<Resp> respType) {
        this.respType = respType;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BytePayload marshall(KeyValue metadata, Req request) {
        assertSerializer();

        Gson gson = gsonSerializer.getGson();
        Charset charset = gsonSerializer.getCharset();
        byte[] metaDataBytes = Conversions.stringToBytes(gson.toJson(metaDataMap(metadata)), charset);
        byte[] dataBytes = Conversions.stringToBytes(gson.toJson(request), charset);
        return new BytePayload(metaDataBytes, dataBytes);
    }

    @Override
    public Resp map(byte[] response) {
        Gson gson = gsonSerializer.getGson();
        String encoding = gsonSerializer.getEncoding();
        return gson.fromJson(
                bytesToReader(
                        response,
                        Charset.forName(encoding)),
                respType);
    }

    @Override
    public void setSerializer(Serializer serializer) {
        if (serializer instanceof GsonSerializer) {
            gsonSerializer = (GsonSerializer) serializer;
        } else {
            throw new IllegalArgumentException(
                    "Expected serializer: GsonSerializer, provided: " + serializer);
        }
    }

    protected GsonSerializer serializer() {
        return gsonSerializer;
    }

    private void assertSerializer() {
        if (gsonSerializer == null) {
            throw new IllegalStateException("Serializer was not set");
        }
    }

    private Map<String, Object> metaDataMap(KeyValue keyValue) {
        Map<String, Object> map = new HashMap<>();
        for (String key : keyValue.keys()) {
            Object val = keyValue.get(key);
            map.put(key, val);
        }
        return map;
    }
}
