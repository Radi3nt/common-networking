package fr.radi3nt.networking.steam.example;

import com.codedisaster.steamworks.*;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.steam.SteamConnection;
import fr.radi3nt.networking.steam.SteamConnectionAcceptor;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class MainSteamServer {

    public static void main(String[] args) throws SteamException {

        SteamAPI.loadLibraries();
        boolean init = SteamAPI.init();
        if (!init)
            throw new RuntimeException("Error initializing SteamAPI");

        System.out.println("creating connection");
        SteamConnectionAcceptor acceptor = new SteamConnectionAcceptor(steamID -> true);
        SteamConnection steamConnection;
        while ((steamConnection=acceptor.accept())==null) {
            SteamAPI.runCallbacks();
        }

        SteamFriends steamFriends = new SteamFriends(new SteamFriendsCallback() {
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
            public void onGameLobbyJoinRequested(SteamID steamID, SteamID steamID1) {
                System.out.println("Lobby join");
            }

            @Override
            public void onAvatarImageLoaded(SteamID steamID, int i, int i1, int i2) {

            }

            @Override
            public void onFriendRichPresenceUpdate(SteamID steamID, int i) {

            }

            @Override
            public void onGameRichPresenceJoinRequested(SteamID steamID, String s) {
                System.out.println("Joined: " + s);
            }

            @Override
            public void onGameServerChangeRequested(String s, String s1) {

            }
        });
        steamFriends.setRichPresence("status", "Waiting for players");
        steamFriends.setRichPresence("connect", "connectKey");
        //SteamConnection steamConnection = new SteamConnection(SteamID.createFromNativeHandle(76561199485014185L));

        System.out.println("connection created");

        CountDownLatch latch = new CountDownLatch(1);
        steamConnection.setListener(buff -> {
            System.out.println("Received: " + Arrays.toString(buff.array()));
            latch.countDown();
        });

        steamConnection.prepare();

        ByteBuffer direct = ByteBuffer.allocateDirect(4);
        direct.putInt(40);
        direct.flip();
        steamConnection.send(direct, ReliabilityFlags.RELIABLE);
        System.out.println("packet sent");
        while (!steamConnection.isInvalid()) {
            steamConnection.update();
            SteamAPI.runCallbacks();
        }
        steamConnection.close();

        SteamAPI.shutdown();
    }

}
