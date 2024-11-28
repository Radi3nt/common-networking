package fr.radi3nt.networking.tcp;

import fr.radi3nt.networking.common.channel.ChannelStreamHolder;
import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.common.connection.listener.ConnectionListener;
import fr.radi3nt.networking.common.connection.listener.DeafenedConnectionListener;
import fr.radi3nt.networking.common.connection.listener.EngagementListener;
import fr.radi3nt.networking.common.protocol.protocols.ChannelConnectionProtocol;
import fr.radi3nt.networking.common.protocol.protocols.ConnectionProtocol;
import fr.radi3nt.networking.common.protocol.protocols.LengthConnectionProtocol;
import fr.radi3nt.networking.common.protocol.setup.Engagements;
import fr.radi3nt.networking.common.protocol.setup.UnrecoverableEngagementException;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class TcpConnection implements Connection {

    private final ChannelStreamHolder channelStreamHolder;
    private final Engagements engagements;
    protected ConnectionProtocol protocol;

    private ConnectionListener listener = DeafenedConnectionListener.INSTANCE;

    private boolean ready;

    protected boolean remotelyClosed;
    private boolean locallyClosed;
    private boolean shutdown;

    public TcpConnection(SocketAddress address, Engagements engagements) throws IOException {
        this(ChannelStreamHolder.establishTcp(address), engagements);
    }

    protected TcpConnection(ChannelStreamHolder holder, Engagements engagements) {
        channelStreamHolder = holder;
        protocol = new LengthConnectionProtocol(new ChannelConnectionProtocol(holder));
        this.engagements = engagements;
    }

    @Override
    public void prepare() {
        ConnectionListener userListener = listener;
        EngagementListener engagementListener = new EngagementListener(engagements, this);
        listener = engagementListener;

        try {
            processEngagements(engagementListener);
        } catch (UnrecoverableEngagementException e) {
            e.printStackTrace();
            shutdown();
            return;
        }

        listener = userListener;
        ready = true;
    }

    private void processEngagements(EngagementListener engagementListener) throws UnrecoverableEngagementException {
        ConnectionProtocol originalProtocol = protocol;

        while (!engagements.isCompleted()) {
            engagements.engage(this);
            update();
            engagementListener.answer();

            originalProtocol = engagements.replace(originalProtocol);
        }

        protocol = originalProtocol;
    }

    @Override
    public void update() {
        try {
            ByteBuffer[] currentlyRead = protocol.read();
            for (ByteBuffer buffer : currentlyRead) {
                listener.receive(buffer);
            }
        } catch (IOException e) {
            triggerRemoteClose();
        }

    }

    @Override
    public void send(ByteBuffer buf, ReliabilityFlags flags) {
        try {
            protocol.write(new ByteBuffer[]{buf});
        } catch (IOException e) {
            triggerRemoteClose();
        }
    }

    private void triggerRemoteClose() {
        remotelyClosed = true;
        ready = true;
    }

    @Override
    public void setListener(ConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void close() {
        locallyClosed = true;

        if (readyToBeShutdown()) {
            shutdown();
        }
    }

    @Override
    public boolean isEstablished() {
        return ready;
    }

    private boolean readyToBeShutdown() {
        return !shutdown;
    }

    @Override
    public void shutdown() {
        try {
            channelStreamHolder.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ready = true;
        locallyClosed = true;
        remotelyClosed = true;
        shutdown = true;
    }

    public ChannelStreamHolder getChannelStreamHolder() {
        return channelStreamHolder;
    }

    @Override
    public boolean isRemotelyClosed() {
        return remotelyClosed;
    }

    @Override
    public boolean isLocallyClosed() {
        return locallyClosed;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }
}
