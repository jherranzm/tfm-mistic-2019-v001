package edu.uoc.mistic.tfm.jherranzm.services;

import android.util.Log;

import java.util.concurrent.ExecutionException;

import edu.uoc.mistic.tfm.jherranzm.config.Constants;
import edu.uoc.mistic.tfm.jherranzm.tasks.remotesymkeytasks.GetServerStatusTask;

public class ServerInfoService {

    private static final String TAG = ServerInfoService.class.getSimpleName();

    public static String getStatusFromServer() throws ExecutionException, InterruptedException {
        String status = "";
        int max_attempts = 10;
        int current_attempt = 0;
        while (!"ACTIVE".equals(status) && current_attempt < max_attempts) {
            GetServerStatusTask getServerStatusTask = new GetServerStatusTask();
            status = getServerStatusTask.execute(Constants.URL_STATUS).get();
            Log.i(TAG, String.format("Server status : [%s]", status));
            current_attempt++;
        }
        return status;
    }
}
