package edu.uoc.mistic.tfm.jherranzm.services;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import edu.uoc.mistic.tfm.jherranzm.model.Invoice;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;
import edu.uoc.mistic.tfm.jherranzm.model.TotalByProviderVO;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.DeleteAllInvoiceDataTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.DeleteInvoiceDataTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.GetAllInvoiceDataByUserTask;
import edu.uoc.mistic.tfm.jherranzm.tasks.invoicedatatasks.GetTotalsByProviderTask;

public class InvoiceDataDataManagerService {

    private static String TAG = InvoiceDataDataManagerService.class.getSimpleName();

    private static Map<String, Invoice> remoteInvoices = new HashMap<>();
    private static Map<String, InvoiceData> localInvoices = new HashMap<>();

    private static InvoiceDataDataManagerService instance;

    public Activity getCallingActivity() {
        return callingActivity;
    }

    public void setCallingActivity(Activity callingActivity) {
        this.callingActivity = callingActivity;
    }

    private Activity callingActivity;

    public static InvoiceDataDataManagerService getInstance(){
        if (instance == null){
            Log.i(TAG, "InvoiceDataDataManagerService initialization: begin...");
            // if instance is null, initialize
            instance = new InvoiceDataDataManagerService();

            Log.i(TAG, "InvoiceDataDataManagerService initialization: end...");
        }else{
            Log.i(TAG, "InvoiceDataDataManagerService already initialized!");
        }
        return instance;
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
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return invoiceDataList;

    }

    public static List<TotalByProviderVO> getTotalsByProvider() {

        List<TotalByProviderVO> totals = new ArrayList<>();
        try {

            GetTotalsByProviderTask getTotalsByProviderTask = new GetTotalsByProviderTask();
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
        };
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
}
