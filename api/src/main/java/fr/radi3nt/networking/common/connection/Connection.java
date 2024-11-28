package fr.radi3nt.networking.common.connection;

import fr.radi3nt.networking.common.connection.listener.ConnectionListener;

import java.nio.ByteBuffer;

public interface Connection {

    void prepare();

    void send(ByteBuffer buf, ReliabilityFlags flags);
    void setListener(ConnectionListener listener);

    void update();

    void close();
    void shutdown();

    boolean isEstablished();
    boolean isLocallyClosed();
    boolean isRemotelyClosed();
    boolean isShutdown();

    default boolean isInvalid() {
        return isShutdown() || isLocallyClosed() || isRemotelyClosed() || !isEstablished();
    }
}
