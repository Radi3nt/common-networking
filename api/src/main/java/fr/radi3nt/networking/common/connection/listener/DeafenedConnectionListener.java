package fr.radi3nt.networking.common.connection.listener;

import java.nio.ByteBuffer;

public class DeafenedConnectionListener implements ConnectionListener {

    public static final DeafenedConnectionListener INSTANCE = new DeafenedConnectionListener();

    @Override
    public void receive(ByteBuffer buff) {

    }
}
