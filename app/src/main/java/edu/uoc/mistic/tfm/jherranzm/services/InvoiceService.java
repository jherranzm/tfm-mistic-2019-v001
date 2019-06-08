package edu.uoc.mistic.tfm.jherranzm.services;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.model.Invoice;
import edu.uoc.mistic.tfm.jherranzm.model.InvoiceData;
import edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks.UploadedInvoicesGetAllTask;

public class InvoiceService {

    private static final String TAG = InvoiceService.class.getSimpleName();

    private static final Map<String, InvoiceData> localInvoices = new HashMap<>();

    public static List<Invoice> getUploadedInvoicesFromServer(Activity activity, String user) {

        List<Invoice> invoices = new ArrayList<>();
        try {
            UploadedInvoicesGetAllTask uploadedInvoicesGetAllTask = new UploadedInvoicesGetAllTask();

            // retrieve data from server
            List<Invoice> remoteInvoices = uploadedInvoicesGetAllTask.execute(Constants.URL_FACTURAS).get();

            List<InvoiceData> _localInvoices = InvoiceDataService.getInvoiceDataFromDatabase(activity, user);
            for(InvoiceData invoiceData : _localInvoices){
                Log.i(TAG, String.format("localInvoice  : %s", invoiceData.getBatchIdentifier()));
                localInvoices.put(invoiceData.getBatchIdentifier(), invoiceData);
            }

            for (Invoice remoteInvoice : remoteInvoices){

                Log.i(TAG, String.format("remoteInvoice  : %s", remoteInvoice.toString()));

                if(localInvoices.containsKey(remoteInvoice.getUid())){
                    remoteInvoice.setInLocalDatabase(true);
                }else{
                    Log.i(TAG, "localInvoices : NOT found ID ->" + remoteInvoice.getUid());
                    remoteInvoice.setInLocalDatabase(false);
                }
                Log.i(TAG, String.format("remoteInvoice  : %s", remoteInvoice.toString()));
                invoices.add(remoteInvoice);
            }

            Log.i(TAG, String.format("uploadedInvoicesGetAllTask : %d", invoices.size()));

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return invoices;
    }

}
