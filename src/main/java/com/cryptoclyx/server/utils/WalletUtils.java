package com.cryptoclyx.server.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Log4j2
@UtilityClass
public class WalletUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * To encrypt a string you should specify string itself and the key which will be using for encryption.
     * @param strToEncrypt
     * @param key
     * @return
     */
    public static String encrypt(String strToEncrypt, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error while encrypting: {}", e.getMessage());
        }
        return null;
    }

    /**
     * To decrypt the string you should specify a string itself and a key by which this string will be decrypted.
     * @param strToDecrypt
     * @param key
     * @return
     */
    public static String decrypt(String strToDecrypt, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("Error while decrypting: {}", e.getMessage());
        }
        return null;
    }
}

