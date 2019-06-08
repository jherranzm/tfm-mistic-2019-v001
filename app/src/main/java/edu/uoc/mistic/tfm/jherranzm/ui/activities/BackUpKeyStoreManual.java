package edu.uoc.mistic.tfm.jherranzm.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.glxn.qrgen.android.QRCode;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import edu.uoc.mistic.tfm.jherranzm.R;
import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.crypto.SymmetricEncryptor;
import edu.uoc.mistic.tfm.jherranzm.tasks.posttasks.PostDataAuthenticatedToUrlTask;
import edu.uoc.mistic.tfm.jherranzm.util.RandomStringGenerator;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;

public class BackUpKeyStoreManual extends AppCompatActivity {

    // Constants
    private static final String TAG = BackUpKeyStoreManual.class.getSimpleName();

    // Security
    private TFMSecurityManager tfmSecurityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up_key_store_manual);

        tfmSecurityManager = TFMSecurityManager.getInstance();

        TextView infoBackUpKeyStore = findViewById(R.id.infoBackUpKeyStore);
        infoBackUpKeyStore.setText("Manual KeyStore Backup");

        executeBackUp();
    }

    private void executeBackUp() {

        saveKeyStoreToServer();
    }

    private void saveKeyStoreToServer() {

        try {

            // Read KeyStore to String
            InputStream isKeyStoreFile = new FileInputStream(tfmSecurityManager.getKeyStoreFile());

            // Encrypt with the user's password
            byte[] byteArraySignedInvoice = IOUtils.toByteArray(isKeyStoreFile);

            RandomStringGenerator rsg = new RandomStringGenerator();

            // IV and Symmetric Key
            String iv = rsg.getRandomString(16);
            Log.i(TAG, String.format("iv     : [%s]", iv));

            String simKey = rsg.getRandomString(16);
            Log.i(TAG, String.format("simKey : [%s]", simKey));

            SymmetricEncryptor simEnc = new SymmetricEncryptor();
            simEnc.setIv(iv);
            simEnc.setKey(simKey);

            String signedInvoiceEncrypted   = simEnc.encrypt(byteArraySignedInvoice);
            Log.i(TAG,signedInvoiceEncrypted);

            Map<String, String> params = new HashMap<>();
            params.put("op", "store");
            params.put("iv", iv);
            params.put("enc", signedInvoiceEncrypted);

            PostDataAuthenticatedToUrlTask getData = new PostDataAuthenticatedToUrlTask(params);

            String res = getData.execute(Constants.URL_KEYSTORE).get();
            Log.i(TAG, String.format("Response from server : %s", res));

            if(isJSONValid(res)){
                JSONObject jsonResponse = new JSONObject(res);
                Log.i(TAG, String.format("isJSONValid : %b", isJSONValid(res)));
                if (jsonResponse.has("token")) {
                    Log.i(TAG, String.format("token : %s", jsonResponse.get("token")));


                    String strBitmap = iv+"||||"+simKey+"||||"+jsonResponse.get("token");
                    Bitmap myBitmap = QRCode.from(strBitmap).bitmap();
                    ImageView myImage = findViewById(R.id.bidiBackUpKeyStore);
                    myImage.setImageBitmap(myBitmap);

                    TextView textView = findViewById(R.id.infoPLeaseCopyThisImage);
                    textView.setText("Please, save this image. " +
                            "With it you'll be able to recover your encripted KeyStore from server. " +
                            "Besides the file you'll need two keys that are informed in this image.");

                }else{
                    Log.e(TAG, String.format("ERROR : Response server is not what is expected!", ""));
                }
            }else{
                Log.e(TAG, String.format("ERROR : Response server is not a valid JSON object!", ""));
            }




        } catch (Exception e) {
            Log.e(TAG,String.format("ERROR : %s: %s", e.getClass().getCanonicalName(), e.getLocalizedMessage()));
        }
    }

    private boolean isJSONValid(String jsonInString ) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
