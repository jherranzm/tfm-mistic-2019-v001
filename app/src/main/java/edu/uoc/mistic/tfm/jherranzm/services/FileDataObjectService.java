package edu.uoc.mistic.tfm.jherranzm.services;

import android.app.Activity;

import java.util.concurrent.ExecutionException;

import edu.uoc.mistic.tfm.jherranzm.tasks.filedataobjecttasks.DeleteAllFileDataObjectTask;

public class FileDataObjectService {

    public static void deleteAllFileDataObject(Activity activity){
        try {
            DeleteAllFileDataObjectTask deleteAllFileDataObjectTask = new DeleteAllFileDataObjectTask(activity);
            deleteAllFileDataObjectTask.execute().get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
