package fr.radi3nt.networking.tcp;

import fr.radi3nt.networking.common.channel.ChannelStreamHolder;
import fr.radi3nt.networking.common.protocol.protocols.DeferredWriteConnectionProtocol;
import fr.radi3nt.networking.common.protocol.setup.Engagements;

import java.io.IOException;
import java.net.SocketAddress;

public class DeferredTcpConnection extends TcpConnection {

    private DeferredWriteConnectionProtocol deferredWriteProtocol;
    private boolean askedClose;

    public DeferredTcpConnection(SocketAddress address, Engagements engagements) throws IOException {
        super(address, engagements);
    }

    public DeferredTcpConnection(ChannelStreamHolder holder, Engagements engagements) {
        super(holder, engagements);
    }

    @Override
    public void prepare() {
        super.prepare();
        protocol = deferredWriteProtocol = new DeferredWriteConnectionProtocol(protocol, false);
    }

    @Override
    public void update() {
        if (deferredWriteProtocol!=null) {
            try {
                deferredWriteProtocol.writeAll();
            } catch (IOException e) {
                remotelyClosed = true;
            }
            if (deferredWriteProtocol.isEmpty() && askedClose)
                super.close();
        }

        super.update();
    }

    @Override
    public void close() {
        if (isInvalid())
            super.close();

        if (deferredWriteProtocol!=null)
            deferredWriteProtocol.discardSilently();
        askedClose = true;
    }
}
