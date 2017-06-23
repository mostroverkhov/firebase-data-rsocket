package com.github.mostroverkhov.firebase_rsocket.internal.mapper;

import com.github.mostroverkhov.firebase_rsocket.internal.codec.DataCodec;
import com.github.mostroverkhov.firebase_rsocket_data.KeyValue;

import java.util.Collection;
import java.util.Optional;
import java.util.Queue;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class ServerMappers implements ServerMapper {
    private final Queue<ServerMapper<?>> mappers;

    public static ServerMappers newInstance(Queue<ServerMapper<?>> mappers) {
        return new ServerMappers(mappers);
    }

    protected ServerMappers(Queue<ServerMapper<?>> mappers) {
        assertMappers(mappers);
        this.mappers = mappers;
    }

    @Override
    public boolean accepts(KeyValue metaData) {
        return true;
    }

    @Override
    public Optional<?> map(KeyValue metadata, byte[] data) {

        Optional<ServerMapper<?>> maybeMapper = mappers
                .stream()
                .filter(mapper -> mapper.accepts(metadata))
                .findFirst();

        Optional<?> maybeOp =
                maybeMapper.flatMap(mapper -> mapper.map(metadata, data));

        return maybeOp;

    }

    @Override
    public byte[] marshall(Object response) {
        return mappers.peek().marshall(response);
    }

    private static void assertMappers(Collection<ServerMapper<?>> mappers) {
        if (mappers == null) {
            throw new IllegalArgumentException("Adapters should not be null");
        }
        if (mappers.isEmpty()) {
            throw new IllegalArgumentException("Adapters should not be empty");
        }
    }

    @Override
    public ServerMappers setDataCodec(DataCodec dataCodec) {
        mappers.forEach(m -> m.setDataCodec(dataCodec));
        return this;
    }
}
