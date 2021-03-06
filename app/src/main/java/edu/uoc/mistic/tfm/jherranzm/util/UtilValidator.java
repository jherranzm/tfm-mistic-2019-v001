package edu.uoc.mistic.tfm.jherranzm.util;

import android.util.Log;

import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class UtilValidator {

    private static final String TAG = UtilValidator.class.getSimpleName();

    private static boolean isValid(Document doc) throws Exception {
        NodeList nl = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("No XML Digital Signature Found, document is discarded");
        }

        Element sigElement = (Element) nl.item(0);
        XMLSignature signature = new XMLSignature(sigElement, "");


        Log.i(TAG, "Begin Find the Signature Element");
        // Find the Signature Element
        XPath xpath = getXPath();

        // http://apache-xml-project.6118.n7.nabble.com/MissingResourceFailureException-td42081.html
        fixIds(doc, xpath);

        KeyInfo ki = signature.getKeyInfo();
        if (ki == null) {
            throw new Exception("KeyInfo NOT found!");
        }else{
            Log.i(TAG, "ki..." + ki.getId());
            X509Certificate cert = ki.getX509Certificate();
            if (cert != null) {
                Log.i(TAG, "cert..." + cert.getSigAlgName());
                return signature.checkSignatureValue(cert);
            }else{

                PublicKey pk = ki.getPublicKey();
                if (pk != null) {
                    Log.i(TAG, "pk..." + pk.getAlgorithm());
                    return signature.checkSignatureValue(pk);
                } else {
                    throw new Exception("PublicKey NOT found!");
                }
            }

        }
    }

    private static XPath getXPath() {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @SuppressWarnings("rawtypes")
            @Override
            public java.util.Iterator getPrefixes(final String namespaceURI) {
                return Collections.singleton("ds").iterator();
            }

            @Override
            public String getPrefix(final String namespaceURI) {
                return "ds";
            }

            @Override
            public String getNamespaceURI(final String prefix) {
                return "http://www.w3.org/2000/09/xmldsig#";
            }
        });
        return xpath;
    }

    private static void fixIds(Document doc, XPath xpath) throws XPathExpressionException {
        // http://apache-xml-project.6118.n7.nabble.com/MissingResourceFailureException-td42081.html
        String expression2 = "//*[@Id]";
        NodeList allIds = (NodeList) xpath.evaluate(expression2, doc, XPathConstants.NODESET);
        Log.i(TAG, "allIds.getLength..." + allIds.getLength());

        for (int i = 0; i < allIds.getLength(); i++) {
            Element id = (Element) allIds.item(i);
            Log.i(TAG, "id..." + id.getTagName());
            id.setIdAttributeNS(null, "Id", true);
            Log.i(TAG, "id..." + id.getTagName());
        }
    }

    public static boolean validateSignedInvoice(final Document doc){
        boolean valid = false;
        try {
            valid = isValid(doc);
        } catch (IOException e) {
            Log.i(TAG, String.format("ERROR IO : %s", e.getLocalizedMessage()));
        } catch (Exception e) {
            Log.i(TAG, String.format("ERROR Generic : %s", e.getLocalizedMessage()));
        }

        return valid;
    }
}
