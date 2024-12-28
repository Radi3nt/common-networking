package fr.radi3nt.networking.steam.policy;

import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;

import java.nio.ByteBuffer;

import static fr.radi3nt.networking.steam.SteamConnection.MAIN_CHANNEL;
import static fr.radi3nt.networking.steam.SteamConnection.RELIABILITY_FLAGS;

public class InstantSteamWritingPolicy implements SteamWritingPolicy {

    private final SteamNetworking networking;
    private final SteamID remote;

    public InstantSteamWritingPolicy(SteamNetworking networking, SteamID remote) {
        this.networking = networking;
        this.remote = remote;
    }

    @Override
    public void write(ByteBuffer direct, ReliabilityFlags flags) throws SteamException {
        networking.sendP2PPacket(remote, direct, RELIABILITY_FLAGS[flags.ordinal()], MAIN_CHANNEL);
    }

    @Override
    public void update() {

    }

}
