package com.hoyo.audiomanager;

import android.content.Context;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Praba on 3/15/2018.
 */

public class AudioManagerUtils {

    public static String getCurrentDateTimeString(){
        return getCurrentDateTimeString("MMddyyhhmss");
    }

    public static String getCurrentDateTimeString(String pattern){
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(pattern, Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }

    public static File getStoragePath(Context context, String audioDirectory){
        String intStorageDirectory = context.getFilesDir().toString();
        return new File(intStorageDirectory, audioDirectory);
    }

    public static File getAudioFilePath(File storagePath, String fileFormat){
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("MMddyyhhmss", Locale.getDefault());
        String currentDateTimeString = simpleDateFormat.format(new Date());
        String fileName = currentDateTimeString+"_audio."+fileFormat;

        return new File(storagePath,fileName);
    }



}
