package fr.radi3nt.networking.common.protocol.setup.compression;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.common.protocol.protocols.ConditionalCompressionConnectionProtocol;
import fr.radi3nt.networking.common.protocol.protocols.ConnectionProtocol;
import fr.radi3nt.networking.common.protocol.setup.SubEngagements;

import java.nio.ByteBuffer;

public class ServerCompressionEngagement implements SubEngagements {

    private final int compressionThreshold;
    private boolean currentEngage;

    public ServerCompressionEngagement(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
    }

    @Override
    public void engage(Connection connection) {
        if (currentEngage)
            return;
        currentEngage = true;

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(compressionThreshold);
        buffer.flip();

        connection.send(buffer, ReliabilityFlags.RELIABLE);
    }

    @Override
    public void answer(Connection connection, ByteBuffer answer) {

    }

    @Override
    public ConnectionProtocol replace(ConnectionProtocol original) {
        if (compressionThreshold<=0)
            return original;
        return new ConditionalCompressionConnectionProtocol(original, compressionThreshold);
    }

    @Override
    public boolean isCompleted() {
        return currentEngage;
    }
}
