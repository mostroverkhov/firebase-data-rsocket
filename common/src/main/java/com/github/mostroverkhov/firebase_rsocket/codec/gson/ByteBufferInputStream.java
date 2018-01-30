package com.github.mostroverkhov.firebase_rsocket.codec.gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
    protected final ByteBuffer byteBuffer;

    public ByteBufferInputStream(ByteBuffer bytebuffer) { byteBuffer = bytebuffer; }

    @Override public int available() { return byteBuffer.remaining(); }
    
    @Override
    public int read() throws IOException { return byteBuffer.hasRemaining() ? (byteBuffer.get() & 0xFF) : -1; }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!byteBuffer.hasRemaining()) return -1;
        len = Math.min(len, byteBuffer.remaining());
        byteBuffer.get(bytes, off, len);
        return len;
    }
}