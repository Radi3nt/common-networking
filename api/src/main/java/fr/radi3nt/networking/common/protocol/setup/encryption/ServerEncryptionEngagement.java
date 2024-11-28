package fr.radi3nt.networking.common.protocol.setup.encryption;

import fr.radi3nt.networking.common.connection.Connection;
import fr.radi3nt.networking.common.connection.ReliabilityFlags;
import fr.radi3nt.networking.common.protocol.protocols.ConnectionProtocol;
import fr.radi3nt.networking.common.protocol.protocols.EncryptionConnectionProtocol;
import fr.radi3nt.networking.common.protocol.setup.SubEngagements;
import fr.radi3nt.networking.common.protocol.setup.UnrecoverableEngagementException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class ServerEncryptionEngagement implements SubEngagements {

    private final int keySize;
    private final int tokenLength;

    private boolean engaged = false;

    private KeyPair pair;
    private byte[] generatedToken;
    private SecretKey receivedKey;

    public ServerEncryptionEngagement(int keySize, int tokenLength) {
        this.keySize = keySize;
        this.tokenLength = tokenLength;
    }


    @Override
    public void engage(Connection connection) throws UnrecoverableEngagementException {
        if (engaged)
            return;

        engaged = true;

        try {
            sendPublicRSAKeyAndToken(connection);
        } catch (NoSuchAlgorithmException e) {
            throw new UnrecoverableEngagementException(e);
        }
    }

    private void sendPublicRSAKeyAndToken(Connection connection) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize);
        pair = generator.generateKeyPair();

        byte[] publicKeyBytes = pair.getPublic().getEncoded();
        generatedToken = generateToken();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + publicKeyBytes.length + Integer.BYTES + generatedToken.length);
        buffer.putInt(publicKeyBytes.length);
        buffer.put(publicKeyBytes);
        buffer.putInt(tokenLength);
        buffer.put(generatedToken);

        buffer.flip();

        connection.send(buffer, ReliabilityFlags.RELIABLE);
    }

    private byte[] generateToken() {
        Random random = new Random();
        byte[] token = new byte[tokenLength];
        random.nextBytes(token);
        return token;
    }

    @Override
    public void answer(Connection connection, ByteBuffer answer) throws UnrecoverableEngagementException {
        try {
            readSharedSecretAndVerifyToken(answer);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new UnrecoverableEngagementException(e);
        }
    }

    @Override
    public ConnectionProtocol replace(ConnectionProtocol original) throws UnrecoverableEngagementException {
        try {
            return EncryptionConnectionProtocol.from(receivedKey, original);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new UnrecoverableEngagementException(e);
        }
    }

    private void readSharedSecretAndVerifyToken(ByteBuffer answer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnrecoverableEngagementException {
        int encryptedKeyLength = answer.getInt();
        byte[] encryptedKey = new byte[encryptedKeyLength];
        answer.get(encryptedKey);

        int encryptedTokenLength = answer.getInt();
        byte[] encryptedToken = new byte[encryptedTokenLength];
        answer.get(encryptedToken);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());

        byte[] decryptedToken = cipher.doFinal(encryptedToken);
        if (!Arrays.equals(decryptedToken, generatedToken)) {
            throw new UnrecoverableEngagementException("Encryption token doesn't match");
        }

        byte[] decryptedKey = cipher.doFinal(encryptedKey);
        receivedKey = new SecretKeySpec(decryptedKey, "AES");
    }

    @Override
    public boolean isCompleted() {
        return receivedKey!=null;
    }
}
