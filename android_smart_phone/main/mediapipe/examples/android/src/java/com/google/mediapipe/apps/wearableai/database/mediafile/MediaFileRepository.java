package com.google.mediapipe.apps.wearableai.database.mediafile;

//originally from MXT: Memory Expansion Tools
//Jeremy Stairs (stairs1) and Cayden Pierce
//https://github.com/stairs1/memory-expansion-tools

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.mediapipe.apps.wearableai.database.phrase.Phrase;

import com.google.mediapipe.apps.wearableai.database.WearableAiRoomDatabase;

public class MediaFileRepository {

    private MediaFileDao mMediaFileDao;

    public MediaFileRepository(Application application) {
        WearableAiRoomDatabase db = WearableAiRoomDatabase.getDatabase(application);
        mMediaFileDao = db.mediaFileDao();
    }

    public void destroy(){
    }

    public MediaFileEntity getClosestMediaFileSnapshot(String mediaType, long timestamp) throws ExecutionException, InterruptedException {

        Callable<MediaFileEntity> callable = new Callable<MediaFileEntity>() {
            @Override
            public MediaFileEntity call() throws Exception {
                return mMediaFileDao.getClosestMediaFileSnapshot(mediaType, timestamp);
            }
        };

        Future<MediaFileEntity> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    public long insert(MediaFileEntity mediaFile) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<Long> insertCallable = () -> mMediaFileDao.insert(mediaFile);
        Future<Long> future = executorService.submit(insertCallable);
        long rowId = 0;
        try{
            rowId = future.get();
        }
        catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }
        return rowId;
    }



}
