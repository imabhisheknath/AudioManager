package com.hoyo.audiomanager;

import java.io.File;

/**
 * Created by Praba on 3/15/2018.
 */

public interface AudioManagerListener {
    void OnSuccess(File audioFile);
    void OnError(String userMessage, String errorMessage, String errorCode);
    void OnWarning(String warningMessage, String warningCode);
}
