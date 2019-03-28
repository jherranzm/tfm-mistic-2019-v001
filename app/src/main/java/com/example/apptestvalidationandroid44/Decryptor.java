package com.example.apptestvalidationandroid44;

import org.spongycastle.cms.CMSEnvelopedData;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.KeyTransRecipientInformation;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipient;

import java.security.PrivateKey;
import java.util.Collection;

public class Decryptor {

     public static byte[] decryptData(byte[] encryptedData, PrivateKey decryptionKey) throws CMSException {

        byte[] decryptedData = null;
        if (null != encryptedData && null != decryptionKey) {
            CMSEnvelopedData envelopedData = new CMSEnvelopedData(encryptedData);

            Collection<RecipientInformation> recipients = envelopedData.getRecipientInfos().getRecipients();
            KeyTransRecipientInformation recipientInfo = (KeyTransRecipientInformation) recipients.iterator().next();
            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(decryptionKey);

            return recipientInfo.getContent(recipient);
        }
        return decryptedData;
    }
}
