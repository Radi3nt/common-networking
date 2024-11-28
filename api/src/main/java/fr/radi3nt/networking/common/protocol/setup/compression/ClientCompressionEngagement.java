package fr.radi3nt.networking.common.protocol.setup.compression;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.protocol.protocols.ConditionalCompressionConnectionProtocol;
import fr.radi3nt.networking.common.protocol.protocols.ConnectionProtocol;
import fr.radi3nt.networking.common.protocol.setup.SubEngagements;

import java.nio.ByteBuffer;

public class ClientCompressionEngagement implements SubEngagements {

    private int compressionThreshold;
    private boolean completed = false;

    @Override
    public void engage(Connection connection) {

    }

    @Override
    public void answer(Connection connection, ByteBuffer answer) {
        compressionThreshold = answer.getInt();
        completed = true;
    }

    @Override
    public ConnectionProtocol replace(ConnectionProtocol original) {
        if (compressionThreshold<=0)
            return original;
        return new ConditionalCompressionConnectionProtocol(original, compressionThreshold);
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
}
