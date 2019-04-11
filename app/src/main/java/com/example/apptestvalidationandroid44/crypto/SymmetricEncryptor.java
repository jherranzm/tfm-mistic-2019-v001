package com.example.apptestvalidationandroid44.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import es.gob.afirma.core.misc.Base64;


public class SymmetricEncryptor extends SymmetricCommon {

    /**
     *
     * @param src
     * @return
     */
	public String encrypt(String src) {

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, generateKey(), generateInitializationVector());
			return Base64.encode(cipher.doFinal(src.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    /**
     *
     * @param src
     * @return
     */
	public String encrypt(byte[] src) {

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, generateKey(), generateInitializationVector());
			return Base64.encode(cipher.doFinal(src));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    /**
     *
     * @param src
     * @param simKey
     * @return
     */
	public String encrypt(String src, String simKey) {

		try {

			SecretKeySpec skey = new SecretKeySpec(simKey.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			return Base64.encode(cipher.doFinal(src.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
