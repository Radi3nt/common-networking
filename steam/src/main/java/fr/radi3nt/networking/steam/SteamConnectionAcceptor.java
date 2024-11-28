package fr.radi3nt.networking.steam;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import fr.radi3nt.networking.common.connection.ConnectionAcceptor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

public class SteamConnectionAcceptor implements ConnectionAcceptor<SteamConnection> {

    private final Queue<SteamConnection> connections = new ConcurrentLinkedQueue<>();
    private final SteamNetworking networking;

    public SteamConnectionAcceptor(Predicate<SteamID> acceptPredicate) {
        this.networking = new SteamNetworking(new SteamNetworkingCallback() {
            @Override
            public void onP2PSessionConnectFail(SteamID steamID, SteamNetworking.P2PSessionError p2PSessionError) {

            }

            @Override
            public void onP2PSessionRequest(SteamID steamID) {
                if (!acceptPredicate.test(steamID))
                    return;
                networking.acceptP2PSessionWithUser(steamID);
                connections.add(SteamConnection.fromRequest(steamID));
            }
        });
    }

    @Override
    public SteamConnection accept() {
        return connections.poll();
    }

    @Override
    public void close() {
        networking.dispose();
    }
}
