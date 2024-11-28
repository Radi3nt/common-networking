package fr.radi3nt.networking.common.channel.stream;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ReadableStream {

    ByteBuffer read() throws IOException;

}
