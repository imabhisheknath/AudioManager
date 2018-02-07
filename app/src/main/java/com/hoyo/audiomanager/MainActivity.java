package com.hoyo.audiomanager;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AudioManager audioManager = new AudioManager(MainActivity.this) {
            @Override
            public void onRecordComplete(String filename) {

                Log.d("myrecord", filename);

                MediaPlayer player = new MediaPlayer();

                try {
                    player.setDataSource(filename);
                    player.prepare();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("Exception of type : " + e.toString());
                    e.printStackTrace();
                }

                player.start();

            }

            @Override
            public void onRecordError(String message) {

                Log.d("mymsg", message);
            }
        };


        audioManager.StartAudio(5000, "1256");
       /* audioManager.StartAudio("1256");
        audioManager.Stop();
*/
    }
}
