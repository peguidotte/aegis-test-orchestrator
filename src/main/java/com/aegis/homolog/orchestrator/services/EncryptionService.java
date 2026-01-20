package com.aegis.homolog.orchestrator.services;

import com.aegis.homolog.orchestrator.config.EncryptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 256;

    /**
     * Fixed salt for key derivation.
     * For MVP this is sufficient.
     * TODO: In production, consider using a unique salt per installation.
     */
    private static final byte[] SALT = "aegis-encryption-salt-v1".getBytes(StandardCharsets.UTF_8);

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom;

    public EncryptionService(EncryptionProperties properties) {
        validateSecretKey(properties.getSecretKey());
        byte[] keyBytes = deriveKey(properties.getSecretKey());
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        this.secureRandom = new SecureRandom();
        log.info("EncryptionService initialized with AES-256-GCM and PBKDF2 key derivation");
    }

    /**
     * Validates that the secret key is properly configured.
     *
     * @throws IllegalStateException if key is missing or too short
     */
    private void validateSecretKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "Encryption secret key is not configured. " +
                    "Set 'aegis.security.encryption.secret-key' property or ENCRYPTION_SECRET_KEY environment variable."
            );
        }
        if (key.length() < 16) {
            throw new IllegalStateException(
                    "Encryption secret key is too short. Minimum 16 characters required, 32 recommended for AES-256."
            );
        }
    }

    /**
     * Encrypts a plaintext string.
     *
     * @param plaintext the text to encrypt
     * @return Base64-encoded encrypted string (IV + ciphertext)
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("Encryption algorithm not available", e);
            throw new EncryptionException("Encryption algorithm not available", e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            log.error("Invalid encryption key or parameters", e);
            throw new EncryptionException("Invalid encryption key or parameters", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("Encryption failed", e);
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts an encrypted string.
     *
     * @param encryptedText Base64-encoded encrypted string (IV + ciphertext)
     * @return the decrypted plaintext
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 encoded data", e);
            throw new EncryptionException("Invalid encrypted data format", e);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("Decryption algorithm not available", e);
            throw new EncryptionException("Decryption algorithm not available", e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            log.error("Invalid decryption key or parameters", e);
            throw new EncryptionException("Invalid decryption key or parameters", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("Decryption failed - data may be corrupted or tampered", e);
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Derives a 256-bit key from the user-provided secret using PBKDF2.
     * This is much more secure than simple padding/truncation.
     *
     * @param secret the user-provided secret key
     * @return a 32-byte (256-bit) derived key
     */
    private byte[] deriveKey(String secret) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    secret.toCharArray(),
                    SALT,
                    PBKDF2_ITERATIONS,
                    KEY_LENGTH_BITS
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("PBKDF2 algorithm not available", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid key specification for PBKDF2", e);
        }
    }

    /**
     * Exception thrown when encryption/decryption fails.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


