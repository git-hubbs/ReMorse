//Authors: Jason Hubbs
//Date Started: 04-10-2019

package com.example.remorse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
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
import android.media.MediaPlayer;

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
        Button switchToReceive = (Button) findViewById(R.id.switchToReceive);
        Button switchToLearner = (Button) findViewById(R.id.switchToLearner);
        final TextView morseText = (TextView) findViewById(R.id.textView2);
        final EditText textBox = (EditText) findViewById(R.id.editText);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final MediaPlayer mp = MediaPlayer.create(this, R.res.beep);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flashMultiplier = (seekBar.getProgress() + 1);
                mp.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
                            waitTimer(350 * flashMultiplier);
                            break;
                        }
                    }
                }
            }
        });

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
    }

    private void callFlashLight(int n){
        String code = morse[n];
        for(int i = 0; i < code.length(); i++){
            if(code.charAt(i) == '.'){
                flashTimer(50 * flashMultiplier);
            }
            else if(code.charAt(i) == '-'){
                flashTimer(150 * flashMultiplier);
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
            Thread.sleep(150 * flashMultiplier);
        } catch (InterruptedException e) {}
    }

    private void soundTimer(int time){

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
