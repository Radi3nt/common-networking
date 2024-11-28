package fr.radi3nt.networking.common.channel.streams;

import fr.radi3nt.networking.common.channel.stream.WritableStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class ChannelWritableStream implements WritableStream {

    private final ByteChannel channel;

    public ChannelWritableStream(ByteChannel channel) {
        this.channel = channel;
    }

    @Override
    public void write(ByteBuffer data) throws IOException {
        channel.write(data);
    }
}
