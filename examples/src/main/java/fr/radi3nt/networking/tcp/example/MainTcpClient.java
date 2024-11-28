package fr.radi3nt.networking.tcp.example;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.common.protocol.setup.Engagements;
import fr.radi3nt.networking.common.protocol.setup.compression.ClientCompressionEngagement;
import fr.radi3nt.networking.common.protocol.setup.encryption.ClientEncryptionEngagement;
import fr.radi3nt.networking.tcp.DeferredTcpConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainTcpClient {

    public static void main(String[] args) throws IOException {
        Connection connection = new DeferredTcpConnection(new InetSocketAddress(args[0], 8080), new Engagements(Arrays.asList(new ClientEncryptionEngagement(192), new ClientCompressionEngagement())));
        connection.prepare();

        connection.setListener(buff -> {
            byte[] result = new byte[buff.remaining()];
            buff.get(result);
            System.out.println(new String(result));
            connection.close();
        });

        ByteBuffer message = ByteBuffer.wrap("Hello World".getBytes());
        for (int i = 0; i < 5; i++) {
            connection.send(message.slice(), ReliabilityFlags.RELIABLE);
        }


        while (!connection.isInvalid()) {
            connection.update();
        }

        connection.close();

    }

}
