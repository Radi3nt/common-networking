package fr.radi3nt.networking.common.protocol.protocols;

import fr.radi3nt.networking.common.channel.ChannelStreamHolder;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ChannelConnectionProtocol implements ConnectionProtocol {

    private final ChannelStreamHolder channelStreamHolder;

    public ChannelConnectionProtocol(ChannelStreamHolder channelStreamHolder) {
        this.channelStreamHolder = channelStreamHolder;
    }

    @Override
    public void write(ByteBuffer[] buffers) throws IOException {
        for (ByteBuffer buffer : buffers) {
            channelStreamHolder.getNativeWrite().write(buffer);
        }
    }

    @Override
    public ByteBuffer[] read() throws IOException {
        ByteBuffer read = channelStreamHolder.getNativeRead().read();
        if (read.limit()==0)
            return new ByteBuffer[0];

        return new ByteBuffer[]{read};
    }
}
