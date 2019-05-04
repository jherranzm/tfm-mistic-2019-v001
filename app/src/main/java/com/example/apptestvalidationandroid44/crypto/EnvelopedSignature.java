package com.example.apptestvalidationandroid44.crypto;

import android.util.Log;

import com.example.apptestvalidationandroid44.util.TFMSecurityManager;
import com.example.apptestvalidationandroid44.util.UtilValidator;

import org.w3c.dom.Document;

import java.io.StringWriter;
import java.security.Provider;
import java.util.Collections;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class EnvelopedSignature {

    // Constants
    private static final String TAG = "LocalInvoicesRAV";

    public static boolean signXMLFile(final Document docToSign) {

        boolean ret = false;
        TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance();

        try {
            String providerName = System.getProperty("jsr105Provider", "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
            if(providerName == null){
                throw new Exception("ERROR : provider jsr105Provider NOT available in system");
            }
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());

            // Create a Reference to the enveloped document
            Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null),
                    Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                    null, null);

            // Create the SignedInfo.
            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));

            // Create a KeyValue containing the DSA PublicKey that was generated
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            KeyValue kv = kif.newKeyValue(tfmSecurityManager.getCertificate().getPublicKey());

            // Create a KeyInfo and add the KeyValue to it
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

            // Instantiate the document to be signed

            // Create a DOMSignContext and specify the DSA PrivateKey and
            // location of the resulting XMLSignature's parent element
            DOMSignContext dsc = new DOMSignContext(tfmSecurityManager.getPrivateKey(), docToSign.getDocumentElement());

            // Create the XMLSignature (but don't sign it yet)
            XMLSignature signature = fac.newXMLSignature(si, ki);

            // Marshal, generate (and sign) the enveloped signature
            signature.sign(dsc);

            printDocToLog(docToSign);

            boolean valid = UtilValidator.isValid(docToSign);
            Log.i(TAG, "UtilValidator.isValid..." + (valid ? "Válida!!" : "No válida..."));

            ret = true;

        } catch (Exception e) {
            Log.i(TAG, e.getClass().getName() + " : " + e.getMessage());
            e.printStackTrace();
        }

        return ret;
    }

    private static void printDocToLog(Document docToSign)
            throws
            TransformerException {

        StringWriter writer = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(docToSign), new StreamResult(writer));

        String xmlString = writer.getBuffer().toString();
        Log.i(TAG, "printDocToLog : ["+xmlString+"]");
    }

}
