//Authors: Jason Hubbs
//Date Started: 04-10-2019

package com.example.remorse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.SoundPool;

import com.example.remorse.Main2Activity;
import com.example.remorse.Main3Activity;
import com.example.remorse.R;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 50;
    private boolean flashLightStatus = false;
    final char[] english = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            ',', '.', '?' };

    String[] morse = { ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..",
            ".---", "-.-", ".-..", "--", "-.", "---", ".---.", "--.-", ".-.",
            "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", ".----",
            "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.",
            "-----", "--..--", ".-.-.-", "..--.." };



    int flashMultiplier = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Button sendMorse = (Button) findViewById(R.id.changeScreens);
        Button sendSound = (Button) findViewById(R.id.button);
        Button switchToReceive = (Button) findViewById(R.id.switchToReceive);
        Button switchToLearner = (Button) findViewById(R.id.switchToLearner);
        final TextView morseText = (TextView) findViewById(R.id.textView2);
        final EditText textBox = (EditText) findViewById(R.id.editText);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);

        Typeface atomic = Typeface.createFromAsset(getAssets(), "fonts/atomic.ttf");
        textBox.setTypeface(atomic);



        switchToReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, Main2Activity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        switchToLearner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, Main3Activity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        sendSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = textBox.getText().toString();
                morseText.setText(content);

                for(int i = 0; i < content.length(); i++){
                    char letter = content.charAt(i);
                    //Travis right here
                    for(int j = 0; j < 39; j++){
                        if(letter == english[j]){
                            callSound(j);
                            waitTimer(50 * flashMultiplier);
                            break;
                        }
                        else if(letter == ' '){
                            waitTimer(175 * flashMultiplier);
                            break;
                        }
                    }
                }
            }
        });

        sendMorse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String content = textBox.getText().toString();
                morseText.setText(content);

                for(int i = 0; i < content.length(); i++){
                    char letter = content.charAt(i);
                    //Travis right here
                    for(int j = 0; j < 39; j++){
                        if(letter == english[j]){
                            callFlashLight(j);
                            break;
                        }
                        else if(letter == ' '){
                            waitTimer(175 * flashMultiplier);
                            break;
                        }
                    }
                }
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flashMultiplier = (seekBar.getProgress() + 1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void callFlashLight(int n){
        String code = morse[n];
        for(int i = 0; i < code.length(); i++){
            if(code.charAt(i) == '.'){
                flashTimer(25 * flashMultiplier);
            }
            else if(code.charAt(i) == '-'){
                flashTimer(75 * flashMultiplier);
            }
            else{
                Toast.makeText(MainActivity.this, "Error in the code array..",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private  void callSound(int n){
        String code = morse[n];
        for(int i = 0; i < code.length(); i++){
            if(code.charAt(i) == '.'){
                soundTimer(25 * flashMultiplier);
            }
            else if(code.charAt(i) == '-'){
                soundTimer(75 * flashMultiplier);
            }
            else{
                Toast.makeText(MainActivity.this, "Error in the code array..",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void flashTimer(int time){
        try {
            flashLightOn();
            Thread.sleep(time);
            flashLightOff();
            Thread.sleep(25 * flashMultiplier);
        } catch (InterruptedException e) {}
    }



    private void soundTimer(final int time){


        //try {
        //    //beep.start();
        //    Thread.sleep(time);
        //    //beep.pause();
        //    Thread.sleep(75 * flashMultiplier);
        //}catch (InterruptedException e) {}

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool soundPool;

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        final int sound1 = soundPool.load(this, R.raw.beep, 1);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                try{
                    soundPool.play(sound1, 0.1f, 0.1f, 0, 0, 1);
                    Thread.sleep(time);
                    soundPool.stop(sound1);
                    soundPool.release();
                    Thread.sleep(25 * flashMultiplier);
                }catch(InterruptedException e){}
            }
        });

    }

    private void waitTimer(int time){
        flashLightOff();
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }

    private void flashLightOn(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
        } catch (CameraAccessException e){}
    }

    private void flashLightOff(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
        } catch (CameraAccessException e){}
    }
}

            /* Code to switch to another activity
                Intent myIntent = new Intent(MainActivity.this, Main2Activity.class);
                MainActivity.this.startActivity(myIntent);
             */
