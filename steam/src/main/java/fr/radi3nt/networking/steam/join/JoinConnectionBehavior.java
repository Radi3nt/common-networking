package fr.radi3nt.networking.steam.join;

import com.codedisaster.steamworks.SteamID;

public interface JoinConnectionBehavior {

    void joinLobby(SteamID invitedBy, SteamID lobby);
    void joinRichPresence(SteamID friend);

}
