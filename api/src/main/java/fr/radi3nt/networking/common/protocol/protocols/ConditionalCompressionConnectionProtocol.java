package fr.radi3nt.networking.common.protocol.protocols;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

public class ConditionalCompressionConnectionProtocol extends CompressionConnectionProtocol {

    private final int minimumSizeForCompression;

    public ConditionalCompressionConnectionProtocol(ConnectionProtocol protocol, int minimumSizeForCompression) {
        super(protocol);
        this.minimumSizeForCompression = minimumSizeForCompression;
    }

    @Override
    public void write(ByteBuffer[] buffer) throws IOException {

        int totalSize = 0;
        for (ByteBuffer byteBuffer : buffer) {
            totalSize+=byteBuffer.remaining();
        }

        ByteBuffer uncompressedLengthBuffer = ByteBuffer.allocate(Integer.BYTES);

        if (totalSize < minimumSizeForCompression) {
            uncompressedLengthBuffer.putInt(0);
            uncompressedLengthBuffer.flip();
            protocol.write(ConnectionProtocolUtil.insert(uncompressedLengthBuffer, buffer));
        } else {
            uncompressedLengthBuffer.putInt(totalSize);
            uncompressedLengthBuffer.flip();

            ByteArrayOutputStream result = compress(buffer);
            protocol.write(new ByteBuffer[] {uncompressedLengthBuffer, ByteBuffer.wrap(result.toByteArray()) });
        }
    }

    @Override
    public ByteBuffer[] read() throws IOException {
        ByteBuffer[] buffer = protocol.read();
        if (buffer.length==0)
            return buffer;

        byte[] cachingArray = new byte[2048];
        ByteBuffer[] results = new ByteBuffer[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            ByteBuffer byteBuffer = buffer[i];
            int uncompressedLength = byteBuffer.getInt();
            if (uncompressedLength==0) {
                results[i] = byteBuffer;
            } else {
                ByteBuffer result = ByteBuffer.allocate(uncompressedLength);
                try {
                    inflateBuffer(byteBuffer, cachingArray, result);
                    result.flip();
                    results[i] = result;
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return results;
    }
}
