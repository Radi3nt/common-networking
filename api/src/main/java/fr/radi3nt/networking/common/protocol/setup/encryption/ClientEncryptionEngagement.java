package fr.radi3nt.networking.common.protocol.setup.encryption;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.common.protocol.protocols.ConnectionProtocol;
import fr.radi3nt.networking.common.protocol.protocols.EncryptionConnectionProtocol;
import fr.radi3nt.networking.common.protocol.setup.SubEngagements;
import fr.radi3nt.networking.common.protocol.setup.UnrecoverableEngagementException;

import javax.crypto.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class ClientEncryptionEngagement implements SubEngagements {

    private final int secretKeySize;

    private Cipher encoding;
    private SecretKey secretKey;
    private byte[] receivedToken;

    public ClientEncryptionEngagement(int secretKeySize) {
        this.secretKeySize = secretKeySize;
    }

    @Override
    public void engage(Connection connection) {

    }

    @Override
    public void answer(Connection connection, ByteBuffer answer) throws UnrecoverableEngagementException {
        try {
            receive(answer);
            send(connection);
        } catch (Exception e) {
            throw new UnrecoverableEngagementException(e);
        }
    }

    @Override
    public ConnectionProtocol replace(ConnectionProtocol original) throws UnrecoverableEngagementException {
        try {
            return EncryptionConnectionProtocol.from(secretKey, original);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new UnrecoverableEngagementException(e);
        }
    }

    private void send(Connection connection) throws NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(secretKeySize);
        secretKey = keyGenerator.generateKey();

        byte[] key = secretKey.getEncoded();
        byte[] encryptedKey = encoding.doFinal(key);
        byte[] encryptedToken = encoding.doFinal(receivedToken);

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + encryptedKey.length + Integer.BYTES + encryptedToken.length);
        buffer.putInt(encryptedKey.length);
        buffer.put(encryptedKey);
        buffer.putInt(encryptedToken.length);
        buffer.put(encryptedToken);
        buffer.flip();

        connection.send(buffer, ReliabilityFlags.RELIABLE);
    }

    private void receive(ByteBuffer answer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        int publicLength = answer.getInt();
        byte[] publicKeyBytes = new byte[publicLength];
        answer.get(publicKeyBytes);
        int tokenLength = answer.getInt();
        receivedToken = new byte[tokenLength];
        answer.get(receivedToken);

        encoding = Cipher.getInstance("RSA");
        PublicKey publicKey =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        encoding.init(Cipher.ENCRYPT_MODE, publicKey);
    }

    @Override
    public boolean isCompleted() {
        return secretKey!=null;
    }
}
