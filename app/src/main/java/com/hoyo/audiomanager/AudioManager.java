package com.hoyo.audiomanager;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.File;

/**
 * Created by Abhishek on 03-02-2018.
 */

public abstract class AudioManager {

    private static String LOG_TITLE = AudioManager.class.getSimpleName();

    public static String E_AUDIO_DIRECTORY_CREATE_FAILED = "E0211";
    public static String E_INIT_MEDIA_RECORDER_FAILED = "E0212";
    public static String E_START_MEDIA_RECORDER_FAILED = "E0213";
    public static String E_START_MEDIA_RECORDER_FAILED_UNKNOWN = "E0214";
    public static String E_NO_AUDIO_FILE = "E0215";

    public static String W_SERVICE_ALREADY_RUNNING = "W0211";


    private static String AUDIO_FILE_FORMAT = "amr";
    private static String INTERNAL_AUDIO_DIR = "Audio";
    /*private static String AUDIO_FILE_NAME_EXT = "_audio";*/

    private Context context;
    private String directoryName;

    private MediaRecorder mMediaRecorder;
    private File mAudioSavePathInDevice = null;

    private AudioManagerListener mAudioManagerListener;

    private boolean isRecording = false;
    private boolean isRecordingStarted = false;
    private boolean isDebug = false;

    private Handler mTimerHandler;

    public AudioManager(Context context) {
        this.context = context;
    }

    /*
        Public Methods ********************
     */

    /**
     *
     * @param listener : Response Listener for Audio Record Status
     * @return returns the instance of this class
     */

    public AudioManager setListener(AudioManagerListener listener){
        this.mAudioManagerListener = listener;
        debugLog("Audio Set Status Listener : Success");
        return this;
    }


    /**
     *
     * @param status : Enable or disable the debug Logs : false - disable; true - enable
     * @return : returns the instance of this class
     */

    public AudioManager setDebugEnabled(boolean status){
        this.isDebug = status;
        debugLog("Audio Debug Status Set to : "+status);
        return this;
    }


    /**
     *  Call this the activities onPause Method
     */

    public void onPause(){
        debugLog("Audio Recording Entering Pause State");
        stopRecording();
    }

    /**
     *
     * @param dirName : Specify If you want to store audio in any particular directory (in internal storage)
     * @return : returns the instance of this class
     *
     * NOTE:
     * Record Audio | If this method has been used to record audio, It has to be manually stopped using stopRecording() method
     */

    public AudioManager recordAudio(String dirName) {
        return recordAudio(-1,dirName);
    }


    /**
     *
     * @param recordTimeMilliSec : Specify the record time of Audio, (stopRecording method will be automatically called after the specified time)
     * @param dirName : Specify If you want to store audio in any particular directory (in internal storage)
     * @return returns the instance of this class
     */
    public AudioManager recordAudio(int recordTimeMilliSec, String dirName) {

        debugLog("Record Audio Request: Time: "+recordTimeMilliSec + " Dir: "+dirName);

        if(!this.isRecording) {
            this.isRecording = true;

            if (dirName != null && dirName.length() > 0) {
                this.directoryName = dirName + "/" + INTERNAL_AUDIO_DIR;
            } else {
                this.directoryName = INTERNAL_AUDIO_DIR;
            }

            debugLog("Audio Recording - Directory ("+directoryName+") Init : Success");

            if (initializeAudio()) {
                debugLog("Audio Recording - Init : Success");
                if (recordTimeMilliSec != -1) {
                    mTimerHandler = new Handler();
                    mTimerHandler.postDelayed(stopRecordingRunnable, recordTimeMilliSec);
                    debugLog("Audio Recording - Stop Handler Init : Success");
                }
            } // Else Part Already handled internally
        } else {
            if(mAudioManagerListener!=null){
                mAudioManagerListener.OnWarning("Audio Recording Service is Already Running",W_SERVICE_ALREADY_RUNNING);
            }
            debugLog("Audio Recording - Init : Warning, Already Running");
        }

        return this;
    }

    /**
     * Call this method to stopRecording the Audio
     * This method will stop the audio recording if running and save the file to the specified directory
     * If No recording, this method will just clear mediRecorder initialization
     */

    public void stopRecording() {

        debugLog("Audio Recording - Stopping");

        if(isRecording){
            isRecording = false;
            if (mMediaRecorder != null) {
                if (isRecordingStarted) {
                    isRecordingStarted = false;
                    mMediaRecorder.stop();

                    debugLog("Audio Recording : Success, Output File : "+mAudioSavePathInDevice.getAbsolutePath());

                    if(mAudioManagerListener !=null){
                        mAudioManagerListener.OnSuccess(mAudioSavePathInDevice);
                    }
                }
                mMediaRecorder.release();
                mMediaRecorder = null;

                debugLog("Audio Recording - Media Recorder DeInit : Success");
            }

            if(mTimerHandler!=null){
                mTimerHandler.removeCallbacks(stopRecordingRunnable);
                mTimerHandler = null;

                debugLog("Audio Recording - Stop Handler DeInit : Success");
            }
        }

    }

    /*
        Private Methods ********************************
     */

    private void initMediaRecorder() throws Exception {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(mAudioSavePathInDevice.getAbsolutePath());
    }


    private Runnable stopRecordingRunnable = new Runnable() {
        @Override
        public void run() {
            debugLog("Audio Recording - Stop Handler : Stopping Audio Recording");
            stopRecording();
        }
    };

    private boolean initializeAudio() {

        File saveFolder = AudioManagerUtils.getStoragePath(context,directoryName);
        // File saveFolder = new File(intStorageDirectory, "Audio");
        if (!saveFolder.exists()) {
            debugLog("Audio Recording - Init Audio : Creating Directory - "+saveFolder.getAbsolutePath());
            if (!saveFolder.mkdirs()) {

                debugLog("Audio Recording - Init Audio : Creating Directory Failed");

                if(mAudioManagerListener !=null){
                    mAudioManagerListener.OnError("An Internal Error Has Occurred while initializing Audio Recorder",
                            "Error While Creating Audio Directory",E_AUDIO_DIRECTORY_CREATE_FAILED);
                }

                stopRecording();
                /*onRecordError("Error while creating saveFolder");*/
                return false;
            }
            debugLog("Audio Recording - Init Audio : Creating Directory Success");
        } else {
            debugLog("Audio Recording - Init Audio : Directory "+saveFolder.getAbsolutePath() + " :- Already Exists ");
        }

        mAudioSavePathInDevice = AudioManagerUtils.getAudioFilePath(saveFolder, AUDIO_FILE_FORMAT);
        debugLog("Audio Recording - Init Audio : Creating Audio File : "+mAudioSavePathInDevice.getAbsolutePath());

        if(!mAudioSavePathInDevice.isFile()) {

            if(mAudioManagerListener !=null){
                mAudioManagerListener.OnError("An Internal Error Has Occurred while initializing Media Recorder",
                        "Error While Creating Audio File. "+ mAudioSavePathInDevice.getAbsolutePath()+" is Not valid File",
                        E_NO_AUDIO_FILE);
            }

            debugLog("Audio Recording - Init Audio : Creating Audio File Failed");

            stopRecording();
            return false;
        }

        debugLog("Audio Recording - Init Audio : Creating Audio File Success");

        try {
            debugLog("Audio Recording - Init Media Recorder");
            initMediaRecorder();
        } catch (Exception ex) {

            if(mAudioManagerListener !=null){
                mAudioManagerListener.OnError("An Internal Error Has Occurred while initializing Media Recorder",
                        ex.getMessage(),
                        E_INIT_MEDIA_RECORDER_FAILED);
            }

            debugLog("Audio Recording - Init Media Recorder : Failed\n\t"+ex.getMessage());

            stopRecording();
            return false;

        }

        debugLog("Audio Recording - Init Media Recorder : Success");


        try {
            debugLog("Audio Recording - Start Recording...");
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isRecordingStarted = true;
        } /*catch (IllegalStateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            if(mAudioManagerListener !=null) {
                mAudioManagerListener.OnError("An Internal Error Has Occurred while starting Media Recorder",
                        e.getMessage(),
                        E_START_MEDIA_RECORDER_FAILED);
            }
            return false;
        } */catch (Exception e) {
            if(mAudioManagerListener !=null) {
                mAudioManagerListener.OnError("An Internal Error Has Occurred while starting Media Recorder",
                        e.getMessage(),
                        E_START_MEDIA_RECORDER_FAILED_UNKNOWN);
            }
            debugLog("Audio Recording - Start Recording : Failed\n\t"+e.getMessage());
            stopRecording();
            return false;
        }

        return true;
    }
    
    private void debugLog(String logMessage){
        if(isDebug){
            Log.e(LOG_TITLE,""+logMessage);
        }
    }
}



