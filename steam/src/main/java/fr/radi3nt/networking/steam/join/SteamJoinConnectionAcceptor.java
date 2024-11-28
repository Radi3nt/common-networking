package fr.radi3nt.networking.steam.join;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;
import fr.radi3nt.networking.common.connection.ConnectionAcceptor;
import fr.radi3nt.networking.steam.SteamConnection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

public class SteamJoinConnectionAcceptor implements ConnectionAcceptor<SteamConnection> {

    private final Queue<SteamConnection> connections = new ConcurrentLinkedQueue<>();
    private final SteamFriends networking;

    public SteamJoinConnectionAcceptor(Predicate<SteamID> acceptPredicate) {
        this.networking = new SteamFriends(new SteamFriendsCallback() {
            @Override
            public void onSetPersonaNameResponse(boolean b, boolean b1, SteamResult steamResult) {

            }

            @Override
            public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange personaChange) {

            }

            @Override
            public void onGameOverlayActivated(boolean b) {

            }

            @Override
            public void onGameLobbyJoinRequested(SteamID steamID, SteamID lobbyId) {
                if (!acceptPredicate.test(steamID))
                    return;
                connections.add(new SteamConnection(steamID));
            }

            @Override
            public void onAvatarImageLoaded(SteamID steamID, int i, int i1, int i2) {

            }

            @Override
            public void onFriendRichPresenceUpdate(SteamID steamID, int i) {

            }

            @Override
            public void onGameRichPresenceJoinRequested(SteamID steamID, String s) {
                if (!acceptPredicate.test(steamID))
                    return;
                connections.add(new SteamConnection(steamID));
            }

            @Override
            public void onGameServerChangeRequested(String s, String s1) {

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
