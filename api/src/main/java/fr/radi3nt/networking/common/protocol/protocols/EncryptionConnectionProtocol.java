package fr.radi3nt.networking.common.protocol.protocols;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class EncryptionConnectionProtocol implements ConnectionProtocol {
    
    private final Cipher encryptionCipher;
    private final Cipher decryptionCipher;

    private final ConnectionProtocol connectionProtocol;

    public static EncryptionConnectionProtocol from(Key sharedSecret, ConnectionProtocol protocol) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher encryptionCipher = Cipher.getInstance("AES");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, sharedSecret);


        Cipher decryptionCipher = Cipher.getInstance("AES");
        decryptionCipher.init(Cipher.DECRYPT_MODE, sharedSecret);

        return new EncryptionConnectionProtocol(encryptionCipher, decryptionCipher, protocol);
    }

    public EncryptionConnectionProtocol(Cipher encryptionCipher, Cipher decryptionCipher, ConnectionProtocol connectionProtocol) {
        this.encryptionCipher = encryptionCipher;
        this.decryptionCipher = decryptionCipher;
        this.connectionProtocol = connectionProtocol;
    }

    @Override
    public void write(ByteBuffer[] buffers) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (ByteBuffer buffer : buffers) {
            byteArrayOutputStream.write(encryptionCipher.update(buffer.array(), buffer.arrayOffset()+buffer.position(), buffer.remaining()));
        }
        try {
            byteArrayOutputStream.write(encryptionCipher.doFinal());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        connectionProtocol.write(new ByteBuffer[]{ByteBuffer.wrap(byteArrayOutputStream.toByteArray())});
    }

    @Override
    public ByteBuffer[] read() throws IOException {
        ByteBuffer[] buffers = connectionProtocol.read();
        return useCipher(buffers, decryptionCipher);
    }

    private ByteBuffer[] useCipher(ByteBuffer[] buffers, Cipher cipher) {
        ByteBuffer[] results = new ByteBuffer[buffers.length];
        for (int i = 0; i < buffers.length; i++) {
            byte[] result;
            try {
                result = cipher.doFinal(buffers[i].array(), buffers[i].arrayOffset()+buffers[i].position(), buffers[i].remaining());
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
            results[i] = ByteBuffer.wrap(result);
        }
        return results;
    }
}
