package fr.radi3nt.networking.common.connection.listener;

import java.nio.ByteBuffer;

public interface ConnectionListener {

    void receive(ByteBuffer buff);

}
