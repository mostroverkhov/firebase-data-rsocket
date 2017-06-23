package com.github.mostroverkhov.firebase_rsocket.internal.codec;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class Codecs {
    private final DataCodec dataCodec;
    private final MetadataCodec metadataCodec;

    public Codecs(DataCodec dataCodec,
                  MetadataCodec metadataCodec) {
        this.dataCodec = dataCodec;
        this.metadataCodec = metadataCodec;
    }

    public DataCodec getDataCodec() {
        return dataCodec;
    }

    public MetadataCodec getMetadataCodec() {
        return metadataCodec;
    }
}
