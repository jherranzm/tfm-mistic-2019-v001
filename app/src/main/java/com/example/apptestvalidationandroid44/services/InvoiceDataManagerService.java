package com.example.apptestvalidationandroid44.services;

import android.util.Log;

import com.example.apptestvalidationandroid44.config.Constants;
import com.example.apptestvalidationandroid44.model.Invoice;
import com.example.apptestvalidationandroid44.model.InvoiceData;
import com.example.apptestvalidationandroid44.tasks.remotesymkeytasks.UploadedInvoicesGetAllTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class InvoiceDataManagerService {

    private static String TAG = "InvoiceDataManagerService";

    private static Map<String, InvoiceData> localInvoices = new HashMap<>();

    public static List<Invoice> getUploadedInvoicesFromServer() {

        List<Invoice> invoices = new ArrayList<>();
        try {
            UploadedInvoicesGetAllTask uploadedInvoicesGetAllTask = new UploadedInvoicesGetAllTask();

            // retrieve data from server
            List<Invoice> remoteInvoices = uploadedInvoicesGetAllTask.execute(Constants.URL_FACTURAS).get();

            List<InvoiceData> _localInvoices = InvoiceDataDataManagerService.getInvoiceDataFromDatabase();
            for(InvoiceData invoiceData : _localInvoices){
                localInvoices.put(invoiceData.getBatchIdentifier(), invoiceData);
            }

            for (Invoice remoteInvoice : remoteInvoices){

                Log.i(TAG, "remoteInvoice  : " + remoteInvoice.toString());

                if(localInvoices.containsKey(remoteInvoice.getUid())){
                    remoteInvoice.setInLocalDatabase(true);
                }else{
                    Log.i(TAG, "localInvoices : NOT found ID ->" + remoteInvoice.getUid());
                }
                invoices.add(remoteInvoice);
            }

            Log.i(TAG, "uploadedInvoicesGetAllTask : " + invoices.size());

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return invoices;
    }
}
