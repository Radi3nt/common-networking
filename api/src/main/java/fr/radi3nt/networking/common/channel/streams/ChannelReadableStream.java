package fr.radi3nt.networking.common.channel.streams;

import fr.radi3nt.networking.common.channel.stream.ReadableStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class ChannelReadableStream implements ReadableStream {

    private final ByteChannel channel;
    private final ByteBuffer cache = ByteBuffer.allocate(1024);

    public ChannelReadableStream(ByteChannel channel) {
        this.channel = channel;
    }

    @Override
    public ByteBuffer read() throws IOException {
        cache.clear();
        int read = channel.read(cache);
        cache.flip();

        if (read==-1)
            throw new IOException("End of stream");

        byte[] arr = new byte[cache.limit()];
        cache.get(arr);
        return ByteBuffer.wrap(arr);
    }
}
