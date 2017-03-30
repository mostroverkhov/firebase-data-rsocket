package com.github.mostroverkhov.firebase_rsocket.request;

import com.google.gson.Gson;
import io.reactivesocket.Payload;
import io.reactivesocket.util.PayloadImpl;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public abstract class BaseMarshallMap<Req, Resp> implements MarshallMap<Req, Resp> {
    private final Gson gson;

    public BaseMarshallMap(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Payload marshallRequest(Req request) {
        return new PayloadImpl(gson.toJson(request));
    }

    Function<? super Throwable, ? extends Publisher<? extends Resp>> mappingError(String msg) {
        return err -> Flowable.error(new ResponseMappingException(msg, err));
    }

    protected Gson gson() {
        return gson;
    }
}
