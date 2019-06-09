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

            localInvoices.clear();

            List<InvoiceData> _localInvoices = InvoiceDataService.getInvoiceDataListFromDatabase(activity, user);
            for(InvoiceData localInvoiceData : _localInvoices){
                Log.i(TAG, String.format("localInvoice  : %s", localInvoiceData.getBatchIdentifier()));
                localInvoices.put(localInvoiceData.getBatchIdentifier(), localInvoiceData);
            }


            // retrieve data from server
            UploadedInvoicesGetAllTask uploadedInvoicesGetAllTask = new UploadedInvoicesGetAllTask();
            List<Invoice> remoteInvoices = uploadedInvoicesGetAllTask.execute(Constants.URL_FACTURAS).get();

            for (Invoice remoteInvoice : remoteInvoices){
                Log.i(TAG, String.format("remoteInvoice.isInLocalDatabase()  : %b", remoteInvoice.isInLocalDatabase()));
                remoteInvoice.setInLocalDatabase(false);
                Log.i(TAG, String.format("remoteInvoice  : %s", remoteInvoice.getUid()));
                Log.i(TAG, String.format("remoteInvoice.isInLocalDatabase()  : %b", remoteInvoice.isInLocalDatabase()));

                if(localInvoices.containsKey(remoteInvoice.getUid())){
                    Log.i(TAG, String.format("localInvoices  : %d - %s",
                            localInvoices.get(remoteInvoice.getUid()).getId(),
                            localInvoices.get(remoteInvoice.getUid()).getBatchIdentifier()
                            )
                    );
                    remoteInvoice.setInLocalDatabase(true);
                }
                Log.i(TAG, String.format("remoteInvoice  : %b", remoteInvoice.isInLocalDatabase()));
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
