package fr.radi3nt.networking.common.protocol.protocols;

import java.nio.ByteBuffer;

public final class ConnectionProtocolUtil {

    public static ByteBuffer[] insert(ByteBuffer start, ByteBuffer[] end) {
        ByteBuffer[] buffers = new ByteBuffer[end.length+1];
        for (int i = 0; i < end.length; i++) {
            ByteBuffer currentBuffer = end[i];
            buffers[i+1] = currentBuffer;
        }
        buffers[0] = start;
        return buffers;
    }

}
