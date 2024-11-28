package fr.radi3nt.networking.tcp;

import fr.radi3nt.networking.common.channel.ChannelStreamHolder;
import fr.radi3nt.networking.common.connection.ConnectionAcceptor;
import fr.radi3nt.networking.common.protocol.setup.Engagements;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

public class TcpConnectionAcceptor implements ConnectionAcceptor<TcpConnection> {

    private final ServerSocketChannel serverChannel;
    private final TcpConnectionSupplier supplier;

    public TcpConnectionAcceptor(ServerSocketChannel serverChannel, TcpConnectionSupplier supplier) {
        this.serverChannel = serverChannel;
        this.supplier = supplier;
    }

    public static TcpConnectionAcceptor establishServerTcp(SocketAddress address, TcpConnectionSupplier supplier) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(address);
        serverChannel.configureBlocking(false);

        return new TcpConnectionAcceptor(serverChannel, supplier);
    }

    @Override
    public TcpConnection accept() {
        try {
            return tryAccept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private TcpConnection tryAccept() throws IOException {
        SocketChannel client = serverChannel.accept();
        if (client==null)
            return null;
        client.configureBlocking(false);
        client.setOption(StandardSocketOptions.SO_REUSEADDR, true);

        return supplier.get(new ChannelStreamHolder(client));
    }

    @Override
    public void close() {
        try {
            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface TcpConnectionSupplier {

        TcpConnection get(ChannelStreamHolder channel);

    }

    public static class NormalTcpConnectionSupplier implements TcpConnectionSupplier {

        private final Supplier<Engagements> engagementsSupplier;

        public NormalTcpConnectionSupplier(Supplier<Engagements> engagementsSupplier) {
            this.engagementsSupplier = engagementsSupplier;
        }

        @Override
        public TcpConnection get(ChannelStreamHolder channel) {
            return new TcpConnection(channel, engagementsSupplier.get());
        }
    }

    public static class DeferredTcpConnectionSupplier implements TcpConnectionSupplier {

        private final Supplier<Engagements> engagementsSupplier;

        public DeferredTcpConnectionSupplier(Supplier<Engagements> engagementsSupplier) {
            this.engagementsSupplier = engagementsSupplier;
        }

        @Override
        public TcpConnection get(ChannelStreamHolder channel) {
            return new DeferredTcpConnection(channel, engagementsSupplier.get());
        }
    }
}
