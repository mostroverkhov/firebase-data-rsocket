package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.google.gson.Gson;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;

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
    public byte[] marshall(Req request) {
        return Conversions.stringToBytes(gson.toJson(request));
    }

    Function<? super Throwable, ? extends Publisher<? extends Resp>> mappingError(String msg) {
        return err -> Flowable.error(new ResponseMappingException(msg, err));
    }

    protected Gson gson() {
        return gson;
    }
}
