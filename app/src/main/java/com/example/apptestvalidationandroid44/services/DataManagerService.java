package com.example.apptestvalidationandroid44.services;

import android.util.Log;

import com.example.apptestvalidationandroid44.config.Constants;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.model.InvoiceData;
import com.example.apptestvalidationandroid44.model.TotalByProviderVO;
import com.example.apptestvalidationandroid44.tasks.invoicedatatasks.DeleteInvoiceDataTask;
import com.example.apptestvalidationandroid44.tasks.invoicedatatasks.GetAllInvoiceDataTask;
import com.example.apptestvalidationandroid44.tasks.invoicedatatasks.GetTotalsByProviderTask;
import com.example.apptestvalidationandroid44.tasks.remotesymkeytasks.GetAllUploadedInvoicesTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DataManagerService {

    private static String TAG = "DataManagerService";

    private static Map<String, Invoice> remoteInvoices = new HashMap<>();
    private static Map<String, InvoiceData> localInvoices = new HashMap<>();


    public static List<Invoice> getUploadedInvoicesFromServer() {

        List<Invoice> invoices = new ArrayList<>();
        try {
            GetAllUploadedInvoicesTask getAllUploadedInvoicesTask = new GetAllUploadedInvoicesTask();

            // retrieve data from server
            List<Invoice> remoteInvoices = getAllUploadedInvoicesTask.execute(Constants.URL_FACTURAS).get();

            getInvoiceDataFromDatabase();

            for (Invoice remoteInvoice : remoteInvoices){
                if(localInvoices.containsKey(remoteInvoice.getUid())){
                    remoteInvoice.setInLocalDatabase(true);
                }
                invoices.add(remoteInvoice);
            }

            Log.i(TAG, "getAllUploadedInvoicesTask : " + invoices.size());

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return invoices;
    }

    public static List<InvoiceData> getInvoiceDataFromDatabase(){

        List<InvoiceData>  invoiceDataList = new ArrayList<>();
        try {
            GetAllInvoiceDataTask getAllInvoiceDataTask = new GetAllInvoiceDataTask();

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

    public static void deleteInvoiceData(InvoiceData invoiceData) {


        try {

            DeleteInvoiceDataTask deleteInvoiceDataTask = new DeleteInvoiceDataTask(invoiceData);
            deleteInvoiceDataTask.execute().get();

        } catch (Exception e) {
            Log.i(TAG, e.getClass().getCanonicalName() + " : " + e.getLocalizedMessage());
            e.printStackTrace();
        };
    }
}
