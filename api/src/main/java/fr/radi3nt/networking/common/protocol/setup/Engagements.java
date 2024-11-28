package fr.radi3nt.networking.common.protocol.setup;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.protocol.protocols.ConnectionProtocol;

import java.nio.ByteBuffer;
import java.util.List;

public class Engagements implements SubEngagements {

    private final List<SubEngagements> subSetups;
    private int engagementIndex;

    public Engagements(List<SubEngagements> subSetups) {
        this.subSetups = subSetups;
    }

    @Override
    public void engage(Connection connection) throws UnrecoverableEngagementException {
        SubEngagements currentEngagement = subSetups.get(engagementIndex);
        currentEngagement.engage(connection);
    }

    @Override
    public void answer(Connection connection, ByteBuffer answer) throws UnrecoverableEngagementException {
        SubEngagements currentEngagement = subSetups.get(engagementIndex);
        currentEngagement.answer(connection, answer);
    }

    @Override
    public ConnectionProtocol replace(ConnectionProtocol original) throws UnrecoverableEngagementException {
        SubEngagements currentEngagement = subSetups.get(engagementIndex);

        if (!currentEngagement.isCompleted())
            return original;

        engagementIndex++;
        return currentEngagement.replace(original);
    }

    @Override
    public boolean isCompleted() {
        return engagementIndex==subSetups.size();
    }
}
