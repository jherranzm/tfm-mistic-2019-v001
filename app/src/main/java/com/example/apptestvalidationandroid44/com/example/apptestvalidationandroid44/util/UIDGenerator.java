package com.example.apptestvalidationandroid44.com.example.apptestvalidationandroid44.util;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UIDGenerator {

    public static String generate(String str){
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] key = md.digest(str.getBytes(StandardCharsets.UTF_8));
                return Base64.encodeToString(key, Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
    }
}
