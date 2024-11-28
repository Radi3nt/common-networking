package fr.radi3nt.networking.tcp.example;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.connection.ConnectionAcceptor;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.common.protocol.setup.Engagements;
import fr.radi3nt.networking.common.protocol.setup.compression.ServerCompressionEngagement;
import fr.radi3nt.networking.common.protocol.setup.encryption.ServerEncryptionEngagement;
import fr.radi3nt.networking.tcp.TcpConnectionAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainTcpServer {

    public static void main(String[] args) throws IOException {

        ConnectionAcceptor<? extends Connection> tcpAcceptor = TcpConnectionAcceptor.establishServerTcp(new InetSocketAddress(args[0], 8080), new TcpConnectionAcceptor.DeferredTcpConnectionSupplier(() -> new Engagements(Arrays.asList(new ServerEncryptionEngagement(2048, 4), new ServerCompressionEngagement(100)))));
        Connection accepted;
        while (true) {
            Connection accept = tcpAcceptor.accept();
            if (accept!=null) {
                accepted = accept;
                tcpAcceptor.close();
                break;
            }
        }

        accepted.prepare();
        accepted.setListener(buff -> {
            byte[] result = new byte[buff.remaining()];
            buff.get(result);
            System.out.println(new String(result));
            accepted.send(ByteBuffer.wrap("Received!".getBytes()), ReliabilityFlags.RELIABLE);
        });

        while (!accepted.isInvalid()) {
            accepted.update();
        }

        accepted.close();
    }

}
