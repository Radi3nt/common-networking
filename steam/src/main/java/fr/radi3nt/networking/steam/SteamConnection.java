package fr.radi3nt.networking.steam;

import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.common.connection.listener.ConnectionListener;
import fr.radi3nt.networking.common.connection.listener.DeafenedConnectionListener;
import fr.radi3nt.networking.steam.policy.DeferredSteamWritingPolicy;
import fr.radi3nt.networking.steam.policy.InstantSteamWritingPolicy;
import fr.radi3nt.networking.steam.policy.SteamWritingPolicy;

import java.nio.ByteBuffer;

public class SteamConnection implements Connection {

    public static final SteamNetworking.P2PSend[] RELIABILITY_FLAGS = new SteamNetworking.P2PSend[] {
            SteamNetworking.P2PSend.Reliable,
            SteamNetworking.P2PSend.Unreliable
    };
    public static final int MAIN_CHANNEL = 0;

    private final SteamID remote;
    private final SteamNetworking networking;

    private final SteamWritingPolicy writingPolicy;

    private ConnectionListener listener = DeafenedConnectionListener.INSTANCE;

    private boolean readyToSend;
    private boolean remotelyOpen;

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
                if (steamID.equals(remote)) {
                    networking.acceptP2PSessionWithUser(steamID);
                    readyToSend = true;
                }
            }
        });
        writingPolicy = new DeferredSteamWritingPolicy(new InstantSteamWritingPolicy(this.networking, this.remote));
    }

    private SteamConnection(SteamID remote, SteamNetworkingCallback networking) {
        this.remote = remote;
        this.networking = new SteamNetworking(networking);
        writingPolicy = new DeferredSteamWritingPolicy(new InstantSteamWritingPolicy(this.networking, this.remote));
    }

    public static SteamConnection fromRequest(SteamID remote) {
        SteamConnection steamConnection = new SteamConnection(remote, null);
        steamConnection.readyToSend = true;
        return steamConnection;
    }

    @Override
    public void prepare() {
        sendPacketToRemote(ByteBuffer.wrap(new byte[0]), ReliabilityFlags.RELIABLE);
    }

    @Override
    public void send(ByteBuffer buf, ReliabilityFlags flags) {
        if (isInvalid())
            return;
        try {
            writingPolicy.write((ByteBuffer) ByteBuffer.allocateDirect(buf.remaining()).put(buf).flip(), flags);
        } catch (SteamException e) {
            e.printStackTrace();
            remoteClose();
        }
    }

    private void sendPacketToRemote(ByteBuffer buf, ReliabilityFlags flags) {
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

        policyUpdate();

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
        remotelyOpen |= connectionActive;

        if (readyToSend && !connecting && !connectionActive) {
            remoteClose();
            return true;
        }

        return !readyToSend;
    }

    private void remoteClose() {
        remotelyClosed = true;
        readyToSend = true;
    }

    @Override
    public void close() {
        locallyClosed = true;
        policyUpdate();
        if (isReadyToBeShutdown())
            shutdown();
    }

    private void policyUpdate() {
        try {
            writingPolicy.update();
        } catch (SteamException e) {
            e.printStackTrace();
            remoteClose();
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;

        System.out.println("Closed session with user " + remote);
        networking.closeP2PSessionWithUser(remote);
        networking.dispose();

        readyToSend = true;
        locallyClosed = true;
        remoteClose();
    }

    private boolean isReadyToBeShutdown() {
        return !shutdown;
    }

    @Override
    public boolean isEstablished() {
        return readyToSend && remotelyOpen;
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
