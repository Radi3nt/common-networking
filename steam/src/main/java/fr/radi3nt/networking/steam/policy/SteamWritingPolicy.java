package fr.radi3nt.networking.steam.policy;

import com.codedisaster.steamworks.SteamException;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;

import java.nio.ByteBuffer;

public interface SteamWritingPolicy {

    void write(ByteBuffer direct, ReliabilityFlags flags) throws SteamException;
    void update() throws SteamException;

}
