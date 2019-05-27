package edu.uoc.mistic.tfm.jherranzm.services;

import android.app.Activity;
import android.util.Log;

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
import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;
import es.facturae.facturae.v3.facturae.Facturae;

public class InvoiceDataDataManagerService {

    private static final String TAG = InvoiceDataDataManagerService.class.getSimpleName();

    private static TFMSecurityManager tfmSecurityManager;

    private WeakReference<Activity> mActivityRef;

    private static final Map<String, InvoiceData> localInvoices = new HashMap<>();

    private static InvoiceDataDataManagerService instance;

    public static InvoiceDataDataManagerService getInstance() {
        return instance;
    }

    public static InvoiceDataDataManagerService getInstance(Activity activity){
        if (instance == null){
            Log.i(TAG, "InvoiceDataDataManagerService initialization: begin...");
            // if instance is null, initialize
            instance = new InvoiceDataDataManagerService();
            tfmSecurityManager = TFMSecurityManager.getInstance();

            instance.init(activity);
            Log.i(TAG, "InvoiceDataDataManagerService initialization: end...");
        }else{
            Log.i(TAG, "InvoiceDataDataManagerService already initialized!");
        }
        return instance;
    }

    private void init(Activity activity){
        mActivityRef = new WeakReference<>(activity);
    }

    public static List<InvoiceData> getInvoiceDataFromDatabase(Activity activity, String user){

        List<InvoiceData>  invoiceDataList = new ArrayList<>();
        try {
            GetAllInvoiceDataByUserTask getAllInvoiceDataTask = new GetAllInvoiceDataByUserTask(activity, user);

            invoiceDataList = getAllInvoiceDataTask.execute().get();
            localInvoices.clear();

            for(InvoiceData invoiceData : invoiceDataList){
                localInvoices.put(invoiceData.getBatchIdentifier(), invoiceData);
            }

            Log.i(TAG, "GetAllInvoiceDataTask : " + invoiceDataList.size());

        } catch (ExecutionException e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return invoiceDataList;

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
            boolean invoiceBackedUp)
            throws
            java.util.concurrent.ExecutionException,
            InterruptedException {

        InvoiceData invoiceData = getInvoiceData(facturae, UIDFacturaHash);
        invoiceData.setUser(tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_LOGGED));
        invoiceData.setBackedUp(invoiceBackedUp);

        GetByBatchIdentifierInvoiceDataTask getByBatchIdentifierInvoiceDataTask = new GetByBatchIdentifierInvoiceDataTask(mActivityRef.get());
        List<InvoiceData> alreadySaved = getByBatchIdentifierInvoiceDataTask.execute(
                invoiceData.getBatchIdentifier(),
                tfmSecurityManager.getUserLoggedDataFromKeyStore(Constants.USER_LOGGED)
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


}
