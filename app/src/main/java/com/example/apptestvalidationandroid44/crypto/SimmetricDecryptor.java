package com.example.apptestvalidationandroid44.crypto;

import org.spongycastle.util.encoders.Base64;

import javax.crypto.Cipher;

public class SimmetricDecryptor  extends SimmetricCommon{
	
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
}
