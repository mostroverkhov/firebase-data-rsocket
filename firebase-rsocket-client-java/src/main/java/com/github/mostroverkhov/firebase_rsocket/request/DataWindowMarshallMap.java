package com.github.mostroverkhov.firebase_rsocket.request;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadRequest;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.read.ReadResponse;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.reactivesocket.Payload;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static com.github.mostroverkhov.firebase_rsocket_data.common.Conversions.payloadReader;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class DataWindowMarshallMap<T> extends BaseMarshallMap<ReadRequest, ReadResponse<T>> {

    private final Class<T> responseType;

    public DataWindowMarshallMap(Gson gson, Class<T> responseType) {
        super(gson);
        this.responseType = responseType;
    }

    @Override
    public Publisher<ReadResponse<T>> mapResponse(Payload response) {
        ReadResponse<T> readResponse = mapRead(
                gson(),
                response,
                responseType);

        return Flowable.just(readResponse)
                .onErrorResumeNext(mappingError("Error while mapping DataWindow response"));
    }

    private static <T> ReadResponse<T> mapRead(Gson gson,
                                               Payload payload,
                                               Class<T> itemType) {
        Reader reader = payloadReader(payload);

        TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
        JsonElement root = getRoot(new JsonReader(reader), adapter);
        JsonObject rootObject = root.getAsJsonObject();
        ReadRequest readRequest = readResponseRequest(gson, rootObject);
        List<T> data = readResponseData(gson, itemType, rootObject);

        return new ReadResponse<>(readRequest, data);
    }

    private static <T> List<T> readResponseData(Gson gson,
                                                Class<T> itemType,
                                                JsonObject rootObject) {
        JsonArray dataListJson = rootObject
                .get("data").getAsJsonArray();

        List<T> data = new ArrayList<>();
        for (JsonElement dataItemJson : dataListJson) {
            T t = gson.fromJson(dataItemJson, itemType);
            data.add(t);
        }
        return data;
    }

    private static ReadRequest readResponseRequest(Gson gson,
                                                   JsonObject rootObject) {
        return gson.fromJson(
                rootObject.get("readRequest").getAsJsonObject(),
                ReadRequest.class);
    }

    private static JsonElement getRoot(JsonReader jsonReader,
                                       TypeAdapter<JsonElement> adapter) {
        JsonElement root;
        try {
            root = adapter.read(jsonReader);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading json input ", e);
        }
        return root;
    }
}
