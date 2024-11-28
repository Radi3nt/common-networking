package fr.radi3nt.networking.common.protocol.setup;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.protocol.protocols.ConnectionProtocol;

import java.nio.ByteBuffer;

public interface SubEngagements {

    void engage(Connection connection) throws UnrecoverableEngagementException;
    void answer(Connection connection, ByteBuffer answer) throws UnrecoverableEngagementException;

    ConnectionProtocol replace(ConnectionProtocol original) throws UnrecoverableEngagementException;

    boolean isCompleted();

}
