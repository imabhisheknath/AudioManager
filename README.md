# AudioManager
Record audio in internal storage
--------------
How To Use
---------
```java

//Add to your Module:app
compile 'com.github.imabhisheknath:AudioManager:v1.0-beta'

//in MainActivity
  
  
  
  // Initialize  AudioManager
  
  //here "123" is directory name where audio will saved.
  
  AudioManager audioManager = new AudioManager(MainActivity.this, "123") {
            @Override
            public void onRecordComplete(String filename) {

                Log.d("myrecord", filename);
                
                //here filename is saved audio path

               
            }

            @Override
            public void onRecordError(String message) {

                Log.d("mymsg", message);
            }
        };


        //you can start audio  by
        audioManager.StartAudio(5000);
        
        //after five secounds it will automatically stop
        
        // you can also start  by
         audioManager.StartAudio();
         //and stop by
          audioManager.Stop();
         
   
        
        

```
