package edu.uoc.mistic.tfm.jherranzm.services;

import android.app.Activity;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderVO;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.DeleteAllInvoiceDataTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.DeleteInvoiceDataTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.GetAllInvoiceDataByUserTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.GetByBatchIdentifierInvoiceDataTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.GetTotalsByProviderTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.InsertInvoiceDataTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.UpdateInvoiceDataTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicetasks.GetInvoiceByIdTask;
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;
import es.facturae.facturae.v3.facturae.Facturae;

public class InvoiceDataService {

    private static final String TAG = InvoiceDataService.class.getSimpleName();

    private static TFMSecurityManager tfmSecurityManager;

    private WeakReference<Activity> mActivityRef;

    private static final Map<String, InvoiceData> localInvoices = new HashMap<>();

    private static InvoiceDataService instance;

    public static InvoiceDataService getInstance() {
        return instance;
    }

    public static InvoiceDataService getInstance(Activity activity){
        if (instance == null){
            Log.i(TAG, "InvoiceDataService initialization: begin...");
            // if instance is null, initialize
            instance = new InvoiceDataService();
            tfmSecurityManager = TFMSecurityManager.getInstance();

            instance.init(activity);
            Log.i(TAG, "InvoiceDataService initialization: end...");
        }else{
            Log.i(TAG, "InvoiceDataService already initialized!");
        }
        return instance;
    }

    private void init(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }

    public static List<InvoiceData> getInvoiceDataFromDatabase(Activity activity, String user){

        List<InvoiceData>  invoiceDataListInLocalDatabase = new ArrayList<>();
        try {
            GetAllInvoiceDataByUserTask getAllInvoiceDataTask = new GetAllInvoiceDataByUserTask(activity, user);

            invoiceDataListInLocalDatabase = getAllInvoiceDataTask.execute().get();
            localInvoices.clear();

            for(InvoiceData invoiceData : invoiceDataListInLocalDatabase){
                Log.i(TAG, "GetAllInvoiceDataTask : " + invoiceData.getBatchIdentifier());
                localInvoices.put(invoiceData.getBatchIdentifier(), invoiceData);
            }

            Log.i(TAG, "GetAllInvoiceDataTask : " + invoiceDataListInLocalDatabase.size());

        } catch (ExecutionException e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return invoiceDataListInLocalDatabase;

    }
    public static List<InvoiceData> getInvoiceDataListFromDatabase(Activity activity, String user){

        List<InvoiceData>  invoiceDataListInLocalDatabase = new ArrayList<>();
        try {
            GetAllInvoiceDataByUserTask getAllInvoiceDataTask = new GetAllInvoiceDataByUserTask(activity, user);
            invoiceDataListInLocalDatabase = getAllInvoiceDataTask.execute().get();
        } catch (ExecutionException e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        Log.i(TAG, String.format("getInvoiceDataListFromDatabase : %d - %s", invoiceDataListInLocalDatabase.size(), user));
        return invoiceDataListInLocalDatabase;

    }

    public static List<TotalByProviderVO> getTotalsByProvider(Activity activity) {

        List<TotalByProviderVO> totals = new ArrayList<>();
        try {

            GetTotalsByProviderTask getTotalsByProviderTask = new GetTotalsByProviderTask(activity);
            totals = getTotalsByProviderTask.execute().get();
            for (TotalByProviderVO total : totals){
                Log.i(TAG, total.toString());
            }

        } catch (Exception e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return totals;

    }

    private static void updateInvoiceData(Activity activity, InvoiceData invoiceData) {

        try {

            UpdateInvoiceDataTask updateInvoiceDataTask = new UpdateInvoiceDataTask(activity, invoiceData);
            updateInvoiceDataTask.execute().get();

        } catch (Exception e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public static void deleteInvoiceData(Activity activity, InvoiceData invoiceData) {

        try {

            DeleteInvoiceDataTask deleteInvoiceDataTask = new DeleteInvoiceDataTask(activity, invoiceData);
            deleteInvoiceDataTask.execute().get();

        } catch (Exception e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public static void deleteAllInvoiceData(Activity activity){
        try {
            DeleteAllInvoiceDataTask deleteInvoiceDataTask = new DeleteAllInvoiceDataTask(activity);
            deleteInvoiceDataTask.execute().get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void saveInvoiceDataInLocalDatabase(
            Facturae facturae,
            String UIDFacturaHash,
            String signedInvoiceFile,
            boolean invoiceBackedUp)
            throws
            java.util.concurrent.ExecutionException,
            InterruptedException {

        InvoiceData invoiceData = getInvoiceData(facturae, UIDFacturaHash);
        invoiceData.setUser(tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_LOGGED));
        invoiceData.setBackedUp(invoiceBackedUp);
        invoiceData.setSignedInvoiceFile(signedInvoiceFile);

        GetByBatchIdentifierInvoiceDataTask getByBatchIdentifierInvoiceDataTask = new GetByBatchIdentifierInvoiceDataTask(mActivityRef.get());
        List<InvoiceData> alreadySaved = getByBatchIdentifierInvoiceDataTask.execute(
                invoiceData.getBatchIdentifier(),
                tfmSecurityManager.getEmailUserLogged()
        ).get();

        if(alreadySaved.size()>0){

            Log.i(TAG, String.format("InvoiceData already in system : nothing to be done!%s", invoiceData.toString()));
            //alertShow(String.format("Local Database: Invoice %s already in system : nothing to be done!", invoiceData.getInvoiceNumber()));
        }else{

            InsertInvoiceDataTask insertInvoiceDataTask = new InsertInvoiceDataTask(mActivityRef.get(), invoiceData);
            InvoiceData invoiceDataInserted = insertInvoiceDataTask.execute().get();
            //infoShow(String.format("Local Database: Invoice %s loaded in system!", invoiceData.getInvoiceNumber()));
            Log.i(TAG, String.format("InvoiceData stored : %s", invoiceDataInserted.toString()));
        }
    }

    private static InvoiceData getInvoiceData(
            Facturae facturae,
            String UIDFacturaHash) {

        InvoiceData invoiceData = new InvoiceData();

        invoiceData.setBatchIdentifier(UIDFacturaHash);

        invoiceData.setTaxIdentificationNumber(facturae.getParties().getSellerParty().getTaxIdentification().getTaxIdentificationNumber());
        invoiceData.setCorporateName(facturae.getParties().getSellerParty().getLegalEntity().getCorporateName());

        invoiceData.setInvoiceNumber(facturae.getInvoices().getInvoiceList().get(0).getInvoiceHeader().getInvoiceNumber());

        invoiceData.setTaxAmount(facturae.getInvoices().getInvoiceList().get(0).getTaxesOutputList().get(0).getTaxAmount().getTotalAmount());
        invoiceData.setTaxBase(facturae.getInvoices().getInvoiceList().get(0).getTaxesOutputList().get(0).getTaxableBase().getTotalAmount());
        invoiceData.setTotalAmount(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getInvoiceTotal());
        invoiceData.setTotalGrossAmount(facturae.getInvoices().getInvoiceList().get(0).getInvoiceTotals().getTotalGrossAmount());

        invoiceData.setIssueDate(facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getIssueDate());
        invoiceData.setStartDate(facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getInvoicingPeriod().getStartDate());
        invoiceData.setEndDate(facturae.getInvoices().getInvoiceList().get(0).getInvoiceIssueData().getInvoicingPeriod().getStartDate());
        return invoiceData;
    }

    public void syncLocalAndRemote(String user){
        localInvoices.clear();

        // localInvoices is already populate
        getInvoiceDataFromDatabase(mActivityRef.get(), user);

        for(String uid : localInvoices.keySet()){
            InvoiceData invoiceData = localInvoices.get(uid);

            if (invoiceData != null) {
                try {
                    invoiceData.setBackedUp(false);
                    String url = Constants.URL_FACTURAS + "/" + uid;
                    GetInvoiceByIdTask getInvoiceByIdTask = new GetInvoiceByIdTask();

                    String res = getInvoiceByIdTask.execute(url).get();
                    Log.i(TAG, String.format("Received from server : %s", res));

                    if(res != null && isJSONValid(res)) {
                        JSONObject jsonResponse = new JSONObject(res);
                        Log.i(TAG, String.format("isJSONValid : %b", isJSONValid(res)));
                        if (jsonResponse.has("uid")) {
                            Log.i(TAG, String.format("Received from server [uid]: %s", jsonResponse.get("uid")));
                            if(jsonResponse.get("uid").equals(uid)){
                                invoiceData.setBackedUp(true);
                            }else{
                                updateInvoiceData(mActivityRef.get(), invoiceData);
                            }
                        }
                    }

                    updateInvoiceData(mActivityRef.get(), invoiceData);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //localInvoices.put(uid, invoiceData);
        }
        getInvoiceDataFromDatabase(mActivityRef.get(), user);

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
