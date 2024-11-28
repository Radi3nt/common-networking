package fr.radi3nt.networking.common.channel;

import fr.radi3nt.networking.common.channel.stream.ReadableStream;
import fr.radi3nt.networking.common.channel.stream.WritableStream;
import fr.radi3nt.networking.common.channel.streams.ChannelReadableStream;
import fr.radi3nt.networking.common.channel.streams.ChannelWritableStream;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public class ChannelStreamHolder {

    private final ByteChannel socket;

    private final ReadableStream nativeRead;
    private final WritableStream nativeWrite;

    public ChannelStreamHolder(ByteChannel socket) {
        this.socket = socket;

        nativeRead = new ChannelReadableStream(socket);
        nativeWrite = new ChannelWritableStream(socket);
    }

    public static ChannelStreamHolder establishTcp(SocketAddress address) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(address);
        channel.configureBlocking(false);

        return new ChannelStreamHolder(channel);
    }

    public static ChannelStreamHolder establishUdp(SocketAddress address) throws IOException {
        DatagramChannel channel = DatagramChannel.open();

        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.bind(address);
        channel.connect(address);
        channel.configureBlocking(false);

        return new ChannelStreamHolder(channel);
    }

    public ReadableStream getNativeRead() {
        return nativeRead;
    }

    public WritableStream getNativeWrite() {
        return nativeWrite;
    }

    public void close() throws IOException {
        socket.close();
    }
}
