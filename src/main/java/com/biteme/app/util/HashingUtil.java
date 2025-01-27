package com.biteme.app.util;

import com.biteme.app.exception.PasswordHashingException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingUtil {

    private HashingUtil() {
        throw new UnsupportedOperationException("Classe di utilità, non può essere istanziata.");
    }

    /**
     * Hasha una password utilizzando SHA-256.
     *
     * @param password la password in chiaro da hashare.
     * @return la password hashata come stringa esadecimale.
     * @throws PasswordHashingException se si verifica un errore durante l'hashing.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new PasswordHashingException("Errore durante l'hashing con SHA-256.", ex);
        }
    }

    /**
     * Converte un array di byte in una rappresentazione esadecimale.
     *
     * @param bytes l'array di byte da convertire.
     * @return una stringa esadecimale.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}