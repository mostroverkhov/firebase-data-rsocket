package com.github.mostroverkhov.firebase_rsocket.internal.codec.gson;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.ClientCodec;
import com.github.mostroverkhov.firebase_rsocket.internal.codec.gson.util.GsonSerializer;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.bytesToReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class GsonClientCodec implements ClientCodec {

    private final GsonSerializer gsonSerializer;

    public GsonClientCodec(GsonSerializer serializer) {
        Objects.requireNonNull(serializer, "serializer");
        this.gsonSerializer = serializer;
    }

    @Override
    public BytePayload encode(KeyValue metadata, Object request) {
        Gson gson = gsonSerializer.getGson();
        Charset charset = gsonSerializer.getCharset();
        byte[] metaDataBytes = Conversions.stringToBytes(gson.toJson(metaDataMap(metadata)), charset);
        byte[] dataBytes = Conversions.stringToBytes(gson.toJson(request), charset);
        return new BytePayload(metaDataBytes, dataBytes);
    }

    @Override
    public <Resp> Resp decode(byte[] data, Class<Resp> type) {
        Gson gson = gsonSerializer.getGson();
        String encoding = gsonSerializer.getEncoding();
        return gson.fromJson(
                bytesToReader(
                        data,
                        Charset.forName(encoding)),
                type);
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
