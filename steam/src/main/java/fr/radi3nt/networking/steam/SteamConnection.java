package fr.radi3nt.networking.steam;

import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.common.connection.listener.ConnectionListener;
import fr.radi3nt.networking.common.connection.listener.DeafenedConnectionListener;

import java.nio.ByteBuffer;

public class SteamConnection implements Connection {

    private static final SteamNetworking.P2PSend[] RELIABILITY_FLAGS = new SteamNetworking.P2PSend[] {
            SteamNetworking.P2PSend.Reliable,
            SteamNetworking.P2PSend.Unreliable
    };
    private static final int MAIN_CHANNEL = 0;

    private final SteamID remote;
    private final SteamNetworking networking;
    private ConnectionListener listener = DeafenedConnectionListener.INSTANCE;

    private boolean ready;

    protected boolean remotelyClosed;
    private boolean locallyClosed;
    private boolean shutdown;

    public SteamConnection(SteamID remote) {
        this.remote = remote;
        this.networking = new SteamNetworking(new SteamNetworkingCallback() {
            @Override
            public void onP2PSessionConnectFail(SteamID steamID, SteamNetworking.P2PSessionError p2PSessionError) {
                System.out.println("Error: " + steamID + p2PSessionError);
                remoteClose();
            }

            @Override
            public void onP2PSessionRequest(SteamID steamID) {
                if (steamID.equals(remote) && !ready) {
                    networking.acceptP2PSessionWithUser(steamID);
                    ready = true;
                }
            }
        });
    }

    private SteamConnection(SteamID remote, SteamNetworking networking) {
        this.remote = remote;
        this.networking = networking;
    }

    static SteamConnection fromRequest(SteamID remote) {
        SteamConnection steamConnection = new SteamConnection(remote);
        steamConnection.ready = true;
        return steamConnection;
    }

    @Override
    public void prepare() {
        send(ByteBuffer.wrap(new byte[0]), ReliabilityFlags.RELIABLE);
    }

    @Override
    public void send(ByteBuffer buf, ReliabilityFlags flags) {
        try {
            ByteBuffer flip = (ByteBuffer) ByteBuffer.allocateDirect(buf.remaining()).put(buf).flip();
            networking.sendP2PPacket(remote, flip, RELIABILITY_FLAGS[flags.ordinal()], MAIN_CHANNEL);
        } catch (SteamException e) {
            e.printStackTrace();
            remoteClose();
        }
    }

    @Override
    public void setListener(ConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void update() {
        if (checkStatus()) return;

        if (isInvalid())
            return;

        int[] messageSize = new int[] {0};
        if (networking.isP2PPacketAvailable(MAIN_CHANNEL, messageSize)) {
            ByteBuffer message = ByteBuffer.allocateDirect(messageSize[0]);
            try {
                int received = networking.readP2PPacket(remote, message, MAIN_CHANNEL);
                message.flip();
                message.limit(received);
                if (received==0)
                    return;

                byte[] messageBytes = new byte[message.remaining()];
                message.get(messageBytes);
                listener.receive(ByteBuffer.wrap(messageBytes));
            } catch (SteamException e) {
                e.printStackTrace();
                remoteClose();
            }
        }
    }

    private boolean checkStatus() {
        SteamNetworking.P2PSessionState sessionState = new SteamNetworking.P2PSessionState();
        networking.getP2PSessionState(remote, sessionState);

        boolean connecting = sessionState.isConnecting();
        boolean connectionActive = sessionState.isConnectionActive();

        if (ready && !connecting && !connectionActive) {
            remoteClose();
            return true;
        }

        if (!ready) {
            if (!connecting)
                ready = true;
            return true;
        }

        return false;
    }

    private void remoteClose() {
        remotelyClosed = true;
        ready = true;
    }

    @Override
    public void close() {
        locallyClosed = true;
        if (isReadyToBeShutdown())
            shutdown();
    }

    @Override
    public void shutdown() {
        networking.closeP2PSessionWithUser(remote);
        networking.dispose();

        ready = true;
        locallyClosed = true;
        remoteClose();
        shutdown = true;
    }

    private boolean isReadyToBeShutdown() {
        return !shutdown;
    }

    @Override
    public boolean isEstablished() {
        return ready;
    }

    @Override
    public boolean isLocallyClosed() {
        return locallyClosed;
    }

    @Override
    public boolean isRemotelyClosed() {
        return remotelyClosed;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }
}
