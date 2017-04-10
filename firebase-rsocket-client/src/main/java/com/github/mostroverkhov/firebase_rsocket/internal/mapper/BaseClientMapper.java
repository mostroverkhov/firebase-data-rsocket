package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.BytePayload;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.google.gson.Gson;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public abstract class BaseClientMapper<Req extends Operation, Resp> implements ClientMapper<Req, Resp> {
    private final Gson gson;

    public BaseClientMapper(Gson gson) {
        this.gson = gson;
    }

    @Override
    public BytePayload marshall(Req request, KeyValue metadata) {

        byte[] metaDataBytes = Conversions.stringToBytes(gson.toJson(metaDataMap(metadata)));
        byte[] dataBytes = Conversions.stringToBytes(gson.toJson(request));
        return new BytePayload(metaDataBytes, dataBytes);
    }

    Function<? super Throwable, ? extends Publisher<? extends Resp>> mappingError(String msg) {
        return err -> Flowable.error(new ResponseMappingException(msg, err));
    }

    protected Gson gson() {
        return gson;
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
