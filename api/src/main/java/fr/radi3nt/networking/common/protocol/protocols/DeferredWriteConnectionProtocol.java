package fr.radi3nt.networking.common.protocol.protocols;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeferredWriteConnectionProtocol implements ConnectionProtocol {

    private final Queue<ByteBuffer[]> toWrite = new ConcurrentLinkedQueue<>();
    private final ConnectionProtocol protocol;
    private final boolean batch;

    private boolean discardSilently;

    public DeferredWriteConnectionProtocol(ConnectionProtocol protocol, boolean batch) {
        this.protocol = protocol;
        this.batch = batch;
    }

    public void writeAll() throws IOException {
        if (batch)
            writeBatch();
        else
            writeSingle();
    }

    private void writeSingle() throws IOException {
        while (!toWrite.isEmpty()) {
            ByteBuffer[] poll = toWrite.poll();
            protocol.write(poll);
        }
    }

    private void writeBatch() throws IOException {
        List<ByteBuffer> allToSend = new ArrayList<>();
        while (!toWrite.isEmpty()) {
            ByteBuffer[] poll = toWrite.poll();
            allToSend.addAll(Arrays.asList(poll));
        }
        protocol.write(allToSend.toArray(new ByteBuffer[0]));
    }

    public boolean isEmpty() {
        return toWrite.isEmpty();
    }

    @Override
    public void write(ByteBuffer[] buffer) throws IOException {
        if (discardSilently)
            return;
        toWrite.add(buffer);
    }

    @Override
    public ByteBuffer[] read() throws IOException {
        return protocol.read();
    }

    public void discardSilently() {
        discardSilently = true;
    }
}
