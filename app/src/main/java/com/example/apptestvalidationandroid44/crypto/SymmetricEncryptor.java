package com.example.apptestvalidationandroid44.crypto;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricEncryptor extends SymmetricCommon {

    /**
     *
     * @param stringToEncriptAndEncode String to encrypt and encode
     * @return String encrypted and encoded in Base64
     */
	public String encrypt(String stringToEncriptAndEncode) {

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, generateKey(), generateInitializationVector());
			//return Base64.encode(cipher.doFinal(stringToEncode.getBytes()));
			return Base64.getEncoder().encodeToString(cipher.doFinal(stringToEncriptAndEncode.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    /**
     *
     * @param byteArrayToEncriptAndEncode Byte Array to encrypt and encode
     * @return String encrypted and encoded in Base64
     */
	public String encrypt(byte[] byteArrayToEncriptAndEncode) {

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, generateKey(), generateInitializationVector());
			//return Base64.encode(cipher.doFinal(stringToEncode));
			return Base64.getEncoder().encodeToString(cipher.doFinal(byteArrayToEncriptAndEncode));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    /**
     *
     * @param stringToEncriptAndEncode String to encrypt with a symmetric key and encode
     * @param simKey Symmetric key
     * @return String encrypted and encoded in Base64
     */
	public String encrypt(String stringToEncriptAndEncode, String simKey) {

		try {

			SecretKeySpec skey = new SecretKeySpec(simKey.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey);
			//return Base64.encode(cipher.doFinal(src.getBytes()));
            return Base64.getEncoder().encodeToString(cipher.doFinal(stringToEncriptAndEncode.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
