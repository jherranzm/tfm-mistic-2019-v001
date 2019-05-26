package edu.uoc.mistic.tfm.jherranzm.crypto;

import org.spongycastle.cms.CMSEnvelopedData;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.KeyTransRecipientInformation;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipient;

import java.security.PrivateKey;
import java.util.Collection;

public class AsymmetricDecryptor {

     public static byte[] decryptData(byte[] encryptedData, PrivateKey decryptionKey) throws CMSException {

        if (null != encryptedData && null != decryptionKey) {
            CMSEnvelopedData envelopedData = new CMSEnvelopedData(encryptedData);

            Collection<RecipientInformation> recipients = envelopedData.getRecipientInfos().getRecipients();
            KeyTransRecipientInformation recipientInfo = (KeyTransRecipientInformation) recipients.iterator().next();
            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(decryptionKey);

            return recipientInfo.getContent(recipient);
        }else{
            return null;
        }
    }
}
