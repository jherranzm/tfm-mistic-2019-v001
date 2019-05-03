package com.example.apptestvalidationandroid44.crypto;

import com.example.apptestvalidationandroid44.util.TFMSecurityManager;
import com.example.apptestvalidationandroid44.util.UtilValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;

import java.io.StringWriter;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.Set;

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
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class EnvelopedSignature {

    private static final String SECURITY_PROVIDER = "BC";
    public final static String PKCS12_PASSWORD = "Th2S5p2rStr4ngP1ss";

    private static Log logger = LogFactory.getLog(EnvelopedSignature.class);


    public static boolean signXMLFile(final Document docToSign) {

        boolean ret = false;
        //String fileOut = fullPathDirToExport+".xsig";
        TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance();

        try {

            Security.addProvider(new BouncyCastleProvider());
            //Security.addProvider(new org.jcp.xml.dsig.internal.dom.XMLDSigRI());

            //String providerName = System.getProperty("jsr105Provider", "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", SECURITY_PROVIDER);

            char[] keystorePassword = PKCS12_PASSWORD.toCharArray();
            char[] keyPassword = PKCS12_PASSWORD.toCharArray();


            Set<String> messageDigest = Security.getAlgorithms("MessageDigest");
            Set<String> algorithms = Security.getAlgorithms("Algorithm");

            String providerName = System.getProperty("jsr105Provider", "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
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

            //OutputStream os = System.out;
            StringWriter writer = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.transform(new DOMSource(docToSign), new StreamResult(writer));

            String xmlString = writer.getBuffer().toString();
            System.out.println(xmlString);
            android.util.Log.i("EnvelopedSignature", "xmlString     : ["+xmlString+"]");


            boolean valid = UtilValidator.isValid(docToSign);
            android.util.Log.i("EnvelopedSignature", "UtilValidator.isValid..." + (valid ? "Válida!!" : "No válida..."));

//            OutputStream osFile = new FileOutputStream(new File(fileOut));
//            TransformerFactory tfFile = TransformerFactory.newInstance();
//            Transformer transFile = tfFile.newTransformer();
//            transFile.transform(new DOMSource(docToSign), new StreamResult(osFile));

            ret = true;

        } catch (Exception e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }

        return ret;
    }

    public static void ListSupportedAlgorithms() {
        String result = "";

        // get all the providers
        Provider[] providers = Security.getProviders();

        for(Provider provider : providers) {
            for(Object ks : provider.keySet()) {
                String k = ks.toString();
                System.out.println(k);
            }
        }

//	    for (int p = 0; p < providers.length; p++) {
//	        // get all service types for a specific provider
//	        Set<Object> ks = providers[p].keySet();
//	        Set<String> servicetypes = new TreeSet<String>();
//	        for (Iterator<Object> it = ks.iterator(); it.hasNext();) {
//	            String k = it.next().toString();
//	            k = k.split(" ")[0];
//	            if (k.startsWith("Alg.Alias."))
//	                k = k.substring(10);
//
//	            servicetypes.add(k.substring(0, k.indexOf('.')));
//	        }
//
//	        // get all algorithms for a specific service type
//	        int s = 1;
//	        for (Iterator<String> its = servicetypes.iterator(); its.hasNext();) {
//	            String stype = its.next();
//	            Set<String> algorithms = new TreeSet<String>();
//	            for (Iterator<Object> it = ks.iterator(); it.hasNext();) {
//		            String k = it.next().toString();
//		            k = k.split(" ")[0];
//		            if (k.startsWith(stype + "."))
//		                algorithms.add(k.substring(stype.length() + 1));
//		            else if (k.startsWith("Alg.Alias." + stype +"."))
//		                algorithms.add(k.substring(stype.length() + 11));
//
//			        int a = 1;
//			        for (Iterator<String> ita = algorithms.iterator(); ita.hasNext();) {
//			            result += ("[P#" + (p + 1) + ":" + providers[p].getName() + "]" +
//			                       "[S#" + s + ":" + stype + "]" +
//			                       "[A#" + a + ":" + ita.next() + "]\n");
//			            a++;
//			        }
//
//			        s++;
//	            }
//	}
    }
}
