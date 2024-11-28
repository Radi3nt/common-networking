package fr.radi3nt.networking.common.channel.stream;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface WritableStream {

    void write(ByteBuffer data) throws IOException;

}
