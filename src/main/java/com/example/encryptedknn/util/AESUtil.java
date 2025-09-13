package com.example.encryptedknn.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {
    private static final String ALGO = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    // Generate a random AES key (128/192/256 bits)
    public static SecretKey generateKey(int bitLength) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGO);
        keyGen.init(bitLength, new SecureRandom());
        return keyGen.generateKey();
    }

    // Generate a random 16-byte IV
    public static byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        return cipher.doFinal(plaintext);
    }

    public static byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(ciphertext);
    }

    // helper: convert raw key bytes to SecretKey
    public static SecretKey keyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, ALGO);
    }

    // helper: convert SecretKey to base64 string for storage
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // helper: convert base64 string back to SecretKey
    public static SecretKey keyFromString(String keyString) {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        return keyFromBytes(keyBytes);
    }
}
