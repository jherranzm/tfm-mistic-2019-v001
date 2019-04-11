package com.example.apptestvalidationandroid44.crypto;

import org.spongycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricDecryptor extends SymmetricCommon {

    /**
     *
     * Mode CBC
     * Decrypt an input String.
     * Decryptor must be initialized with proper key and IV
     *
     * @param src
     * @return
     */
	public String decrypt(String src) {
		String decrypted;
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, generateKey(), generateInitializationVector());
			decrypted = new String(cipher.doFinal(Base64.decode(src)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return decrypted;
	}

    /**
     *
     * Mode CBC
     * Decrypt an input byte array.
     * Decryptor must be initialized with proper key and IV
     *
     * @param src
     * @return
     */
	public byte[] decrypt(byte[] src) {
		byte[] decrypted;
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, generateKey(), generateInitializationVector());
			decrypted = cipher.doFinal(Base64.decode(src));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return decrypted;
	}


    /**
     *
     * Mode ECB
     * Decrypt an input String with the key provided as a parameter.
     *
     * @param src
     * @param simKey
     * @return
     */
	public String decrypt(String src, String simKey) {
		String decrypted;
		try {
			SecretKeySpec skey = new SecretKeySpec(simKey.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skey);
			decrypted = new String(cipher.doFinal(Base64.decode(src)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return decrypted;
	}
}
