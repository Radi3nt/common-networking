package fr.radi3nt.networking.steam.policy;

import com.codedisaster.steamworks.SteamException;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeferredSteamWritingPolicy implements SteamWritingPolicy {

    private final ConcurrentLinkedQueue<SteamMessage> queue = new ConcurrentLinkedQueue<>();
    private final InstantSteamWritingPolicy instantSteamWritingPolicy;

    public DeferredSteamWritingPolicy(InstantSteamWritingPolicy instantSteamWritingPolicy) {
        this.instantSteamWritingPolicy = instantSteamWritingPolicy;
    }

    @Override
    public void write(ByteBuffer direct, ReliabilityFlags flags) {
        queue.add(new SteamMessage(direct, flags));
    }

    @Override
    public void update() throws SteamException {
        while (!queue.isEmpty()) {
            SteamMessage poll = queue.poll();
            instantSteamWritingPolicy.write(poll.direct, poll.flags);
        }
    }

    private static class SteamMessage {

        public final ByteBuffer direct;
        public final ReliabilityFlags flags;

        private SteamMessage(ByteBuffer direct, ReliabilityFlags flags) {
            this.direct = direct;
            this.flags = flags;
        }

        public ByteBuffer getDirect() {
            return direct;
        }

        public ReliabilityFlags getFlags() {
            return flags;
        }
    }
}
