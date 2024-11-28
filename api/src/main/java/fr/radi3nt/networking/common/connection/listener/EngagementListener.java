package fr.radi3nt.networking.common.connection.listener;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.protocol.setup.Engagements;
import fr.radi3nt.networking.common.protocol.setup.UnrecoverableEngagementException;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class EngagementListener implements ConnectionListener {

    private final Engagements engagements;
    private final Connection connection;

    private final Queue<ByteBuffer> pendingBuffers = new ArrayDeque<>();

    public EngagementListener(Engagements engagements, Connection connection) {
        this.engagements = engagements;
        this.connection = connection;
    }

    @Override
    public void receive(ByteBuffer buff) {
        pendingBuffers.add(buff);
    }

    public void answer() throws UnrecoverableEngagementException {
        while (!pendingBuffers.isEmpty()) {
            ByteBuffer poll = pendingBuffers.poll();
            engagements.answer(connection, poll);
        }
    }
}
