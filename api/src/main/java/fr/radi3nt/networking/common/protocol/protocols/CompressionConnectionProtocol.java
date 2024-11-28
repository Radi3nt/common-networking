package fr.radi3nt.networking.common.protocol.protocols;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionConnectionProtocol implements ConnectionProtocol {

    private final Inflater inflater = new Inflater();
    private final Deflater deflater = new Deflater();

    protected final ConnectionProtocol protocol;

    public CompressionConnectionProtocol(ConnectionProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public void write(ByteBuffer[] buffer) throws IOException {
        ByteArrayOutputStream result = compress(buffer);

        protocol.write(new ByteBuffer[] { ByteBuffer.wrap(result.toByteArray()) });
    }

    protected ByteArrayOutputStream compress(ByteBuffer[] buffer) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        for (ByteBuffer byteBuffer : buffer) {
            data.write(byteBuffer.array(), byteBuffer.arrayOffset()+byteBuffer.position(), byteBuffer.remaining());
        }
        deflater.reset();
        deflater.setInput(data.toByteArray());
        deflater.finish();

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] cachingArray = new byte[2048];
        while(!this.deflater.finished()) {
            int actualBytesSize = this.deflater.deflate(cachingArray, 0, cachingArray.length);
            result.write(cachingArray, 0, actualBytesSize);
        }

        return result;
    }

    @Override
    public ByteBuffer[] read() throws IOException {
        ByteBuffer[] read = protocol.read();
        if (read.length==0)
            return read;

        ByteBuffer[] results = new ByteBuffer[read.length];

        byte[] cachingArray = new byte[2048];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (int i = 0, readLength = read.length; i < readLength; i++) {
            results[i] = inflateBuffer(read[i], result, cachingArray);
        }

        return results;
    }

    protected ByteBuffer inflateBuffer(ByteBuffer buffer, ByteArrayOutputStream result, byte[] cachingArray) {
        result.reset();
        try {
            return inflateBuffer(buffer, cachingArray, result);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ByteBuffer inflateBuffer(ByteBuffer buffer, byte[] cachingArray, ByteArrayOutputStream result) throws DataFormatException {
        inflater.reset();
        inflater.setInput(buffer.array(), buffer.arrayOffset()+buffer.position(), buffer.remaining());
        while (!this.inflater.finished()) {
            int actualBytesSize = this.inflater.inflate(cachingArray, 0, cachingArray.length);
            result.write(cachingArray, 0, actualBytesSize);
        }
        return ByteBuffer.wrap(result.toByteArray());
    }

    protected void inflateBuffer(ByteBuffer buffer, byte[] cachingArray, ByteBuffer result) throws DataFormatException {
        inflater.reset();
        System.out.println( );
        inflater.setInput(buffer.array(), buffer.arrayOffset()+buffer.position(), buffer.remaining());
        while (!this.inflater.finished()) {
            int actualBytesSize = this.inflater.inflate(cachingArray, 0, cachingArray.length);
            result.put(cachingArray, 0, actualBytesSize);
        }
    }
}
