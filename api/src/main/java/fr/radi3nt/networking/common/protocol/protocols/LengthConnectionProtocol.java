package fr.radi3nt.networking.common.protocol.protocols;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LengthConnectionProtocol implements ConnectionProtocol {

    private final ConnectionProtocol protocol;

    private ByteBuffer reading;

    public LengthConnectionProtocol(ConnectionProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public void write(ByteBuffer[] buffer) throws IOException {
        ByteBuffer[] buffers = new ByteBuffer[buffer.length+1];
        int totalSize = 0;
        for (int i = 0; i < buffer.length; i++) {
            ByteBuffer currentBuffer = buffer[i];
            buffers[i+1] = currentBuffer;
            totalSize+=currentBuffer.remaining();
        }
        buffers[0] = ByteBuffer.allocate(Integer.BYTES);
        buffers[0].putInt(totalSize);
        buffers[0].flip();
        protocol.write(buffers);
    }

    @Override
    public ByteBuffer[] read() throws IOException {
        ByteBuffer[] read = protocol.read();
        if (read.length==0)
            return read;

        List<ByteBuffer> completed = new ArrayList<>();
        for (ByteBuffer buffer : read) {
            while (buffer.remaining()>0) {
                if (reading==null) {
                    int totalSize = buffer.getInt();
                    reading = ByteBuffer.allocate(totalSize);
                }
                readRemaining(buffer);

                if (reading!=null && reading.remaining()==0) {
                    reading.flip();
                    completed.add(reading);
                    reading = null;
                }
            }
        }

        return completed.toArray(new ByteBuffer[0]);
    }

    private void readRemaining(ByteBuffer buffer) {
        int initialLimit = buffer.limit();
        buffer.limit(Math.min(reading.remaining()+buffer.position(), initialLimit));
        reading.put(buffer);
        buffer.limit(initialLimit);
    }
}
