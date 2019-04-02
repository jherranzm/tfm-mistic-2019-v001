package com.example.apptestvalidationandroid44.crypto;

import javax.crypto.Cipher;

import es.gob.afirma.core.misc.Base64;


public class SimmetricEncryptor extends SimmetricCommon{
	
	public String encrypt(String src) {
		
	    try {
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, generateKey(), generateInitializationVector());
	        return Base64.encode(cipher.doFinal(src.getBytes()));
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
