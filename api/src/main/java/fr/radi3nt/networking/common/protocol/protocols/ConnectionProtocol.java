package fr.radi3nt.networking.common.protocol.protocols;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ConnectionProtocol {

    void write(ByteBuffer[] buffer) throws IOException;
    ByteBuffer[] read() throws IOException;

}
