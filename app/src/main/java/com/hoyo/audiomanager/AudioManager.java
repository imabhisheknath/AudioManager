package com.hoyo.audiomanager;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Administrator on 03-02-2018.
 */

public abstract class AudioManager {
    private Context context;
    private boolean flag = false;
    private String directoryName;

    private MediaRecorder mediaRecorder;
    private String AudioSavePathInDevice = null;


    public abstract void onRecordComplete(String filename);
    public abstract void onRecordError(String message);

    public AudioManager(Context context) {
        this.context = context;
    }


    private void MediaRecorderReady() throws Exception {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }


    private Runnable stopRecordingRunnable = new Runnable() {
        @Override
        public void run() {
            Stop();
        }
    };


    public void onPause(){
        Stop();
    }

    public void StartAudio(String dirName) {
        StartAudio(-1,dirName);
    }


    public void StartAudio(int time, String dirName) {
        this.directoryName = dirName;

        if(initializeAudio()){
            if(time!=-1) {
                new Handler().postDelayed(stopRecordingRunnable, time);
            }
        } // Else Part Already handled internally
    }


    public void Stop() {

        if (mediaRecorder != null) {
            if (flag) {
                mediaRecorder.stop();
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
        try {
            if (AudioSavePathInDevice == null) {
                onRecordError("No path created for audio");
            } else {
                onRecordComplete(AudioSavePathInDevice);
            }
        } catch (Exception ex) {

            onRecordError(ex.getMessage());
        }
    }


    private boolean initializeAudio() {

        File folder = this.getOutputPath();

        // File folder = new File(intStorageDirectory, "Audio");
        if (!folder.exists()) {
            boolean wasSuccessful = folder.mkdirs();
            if (!wasSuccessful) {
                onRecordError("Error while creating folder");
                return false;
            }
        }

        AudioSavePathInDevice = this.getOutputFileName(folder);

        try {
            MediaRecorderReady();
        } catch (Exception ex) {
            onRecordError(ex.getMessage());
            return false;
        }


        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            flag = true;
        } catch (IllegalStateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            onRecordError(e.getMessage());
            return false;
        } catch (Exception e) {
            onRecordError(e.getMessage());
            return false;
        }

        return true;
    }

    private File getOutputPath(){
        String intStorageDirectory = context.getFilesDir().toString();
        return new File(intStorageDirectory, directoryName + "/" + "Audio");
    }
    private String getOutputFileName(File outputPath){
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("MMddyyhhmss", Locale.getDefault());
        String currentDateTimeString = simpleDateFormat.format(new Date());
        return outputPath + "/" + currentDateTimeString + "_audio.amr";
    }


/*    //random file creation

    private String CreateRandomAudioFileName(int string) {
        String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
        Random random = new Random();
        try {

            StringBuilder stringBuilder = new StringBuilder(string);

            int i = 0;
            while (i < string) {

                stringBuilder.append(RandomAudioFileName.charAt(random.nextInt(RandomAudioFileName.length())));

                i++;
            }
            return stringBuilder.toString();

        } catch (Exception ex) {
            onRecordError(ex.getMessage());
            return null;
        }
    }*/


}



