package com.github.mostroverkhov.firebase_rsocket.internal.codec;

import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;
import com.github.mostroverkhov.firebase_rsocket_data.common.Conversions;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class GsonMetadataCodec implements MetadataCodec {

    @Override
    public byte[] encode(KeyValue keyValue) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public KeyValue decode(byte[] metadata) {
        try (JsonReader reader = new JsonReader(Conversions.bytesToReader(metadata))) {
            KeyValue kv = new KeyValue();
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                String val = reader.nextString();
                kv.put(key, val);
            }
            return kv;
        } catch (IOException e) {
            throw mapMetadataError(metadata);
        }
    }

    private static IllegalStateException mapMetadataError(byte[] metaData) {
        return new IllegalStateException("Error reading metadata op: " + bytesToMessage(metaData));
    }

    private static String bytesToMessage(byte[] data) {
        String s;
        try {
            s = IOUtils.toString(data, "UTF-8");
        } catch (IOException e) {
            s = "(error reading message as UTF-8 string)";
        }
        return s;
    }
}
