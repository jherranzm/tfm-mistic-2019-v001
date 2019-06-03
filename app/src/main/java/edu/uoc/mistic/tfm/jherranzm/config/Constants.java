package edu.uoc.mistic.tfm.jherranzm.config;

public class Constants {


    public static final int SERVER_ACTIVE = 1;
    public static final int SERVER_INACTIVE = 0;
    private static final String REMOTE_HOST = "10.0.2.2";
    private static final String REMOTE_PORT = "8443";

    public static final String URL_FACTURAS = "https://"+ REMOTE_HOST+":"+REMOTE_PORT+"/facturas";
    public static final String URL_KEYS     = "https://"+ REMOTE_HOST+":"+REMOTE_PORT+"/keys";
    public static final String URL_CERT     = "https://"+ REMOTE_HOST+":"+REMOTE_PORT+"/cert/csr";
    public static final String URL_STATUS   = "https://"+ REMOTE_HOST+":"+REMOTE_PORT+"/status";
    public static final String URL_SIGNUP   = "https://"+ REMOTE_HOST+":"+REMOTE_PORT+"/signup";
    public static final String URL_LOGIN    = "https://"+ REMOTE_HOST+":"+REMOTE_PORT+"/login";
    public static final String URL_KEYSTORE = "https://"+ REMOTE_HOST+":"+REMOTE_PORT+"/ksb";

    public static final String UID_FACTURA                  = "f1";
    public static final String TAX_IDENTIFICATION_NUMBER    = "f2";
    public static final String CORPORATE_NAME               = "f3";
    public static final String INVOICE_NUMBER               = "f4";
    public static final String INVOICE_TOTAL                = "f5";
    public static final String TOTAL_GROSS_AMOUNT           = "f6";
    public static final String TOTAL_TAX_OUTPUTS            = "f7";
    public static final String ISSUE_DATE                   = "f8";

    public static final String CR_LF = "\n";
    public static final String BC = "BC";


    public static final String PKCS_12 = "PKCS12";
    public static final String PKCS12_PASSWORD = "Th2S5p2rStr4ngP1ss";
    public static final String X_509 = "X.509";
    public static final String SERVER_CERTIFICATE_FILE = "server.crt";
    public static final String SERVER_KEY_P12 = "serverkey.p12";
    public static final String CA_CERTIFICATE_FILE = "ca.crt";

    public static final String AES = "AES";

    public static final String KEY_STORE_BKS_FILE = "keyStoreInvoiceApp.bks";
    public static final String USER_LOGGED = "userLogged";
    public static final String USER_PASS = "userPass";
    public static final String USER_CORRECTLY_REGISTERED_IN_SYSTEM = "User correctly registered in system";
    public static final String USER_ALREADY_REGISTERED_IN_SYSTEM = "email already registered in system";
}
