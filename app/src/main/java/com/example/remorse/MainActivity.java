//Authors: Jason Hubbs
//Date Started: 04-10-2019

package com.example.remorse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioAttributes;
import android.media.Image;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.SoundPool;
import java.util.concurrent.Future;
//import android.widget.S

import com.example.remorse.Main2Activity;
import com.example.remorse.Main3Activity;
import com.example.remorse.R;

import org.opencv.android.OpenCVLoader;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class MainActivity extends AppCompatActivity {



    private static final int CAMERA_REQUEST = 50;
    private boolean flashLightStatus = false;
    final char[] english = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            ',', '.', '?'};

    final String[] morse = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..",
            ".---", "-.-", ".-..", "--", "-.", "---", ".---.", "--.-", ".-.",
            "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", ".----",
            "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.",
            "-----", "--..--", ".-.-.-", "..--.."};


    int flashMultiplier = 4;
    boolean flash = false;
    boolean sound = false;



    boolean stopFlashThread = false;
    boolean stopSoundThread = false;

    String morseText = "";
    String text;

    ImageButton powerButton;

    public void changePowerButtonSetting(){
        powerButton.setImageResource(R.drawable.power);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText textBox = (EditText) findViewById(R.id.editText);
        final TextView morseView = (TextView) findViewById(R.id.textView3);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final ImageButton menu = (ImageButton) findViewById(R.id.imageButton3);
        final ImageButton lightButton = (ImageButton) findViewById(R.id.lightButton);
        final ImageButton soundButton = (ImageButton) findViewById(R.id.soundButton);
        final TextView flashView = (TextView) findViewById(R.id.flashView);
        final TextView soundView = (TextView) findViewById(R.id.soundView);
        powerButton = (ImageButton) findViewById(R.id.powerButton);


        Typeface atomic = Typeface.createFromAsset(getAssets(), "fonts/andale-mono.ttf");
        Typeface morse = Typeface.createFromAsset(getAssets(), "fonts/morse.ttf");
        textBox.setTypeface(atomic);
        morseView.setTypeface(morse);

        textBox.requestFocus();


        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sound) {
                    text = textBox.getText().toString();
                    SoundRunnable soundRunnable = new SoundRunnable(text);
                    new Thread(soundRunnable).start();
                    powerButton.setImageResource(R.drawable.poweron);

                }
                if (flash) {
                    text = textBox.getText().toString();
                    FlashRunnable flashRunnable = new FlashRunnable(text);
                    new Thread(flashRunnable).start();
                    powerButton.setImageResource(R.drawable.poweron);
                }
            }
        });

        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sound){
                    sound = false;
                    soundButton.setImageResource(R.drawable.sound);
                    soundView.setText("Sound: Off");
                }
                else{
                    sound = true;
                    soundButton.setImageResource(R.drawable.soundon);
                    soundView.setText("Sound: On");
                }
            }
        });

        lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flash) {
                    flash = false;
                    lightButton.setImageResource(R.drawable.flash);
                    flashView.setText("Flash: Off");
                }
                else{
                    flash = true;
                    lightButton.setImageResource(R.drawable.flashon);
                    flashView.setText("Flash: On");
                }
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, menu);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.learnActivity:
                                Intent learnIntent = new Intent(MainActivity.this, Main2Activity.class);
                                MainActivity.this.startActivity(learnIntent);
                                return true;
                            case R.id.ReceiveActivity:
                                Intent receiveIntent = new Intent(MainActivity.this, Main3Activity.class);
                                MainActivity.this.startActivity(receiveIntent);
                                return true;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        textBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                morseView.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //transmit.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        if (sound) {
        //            text = textBox.getText().toString();
        //            SoundRunnable soundRunnable = new SoundRunnable(text);
        //            new Thread(soundRunnable).start();
        //        }
        //        if (flash) {
        //            text = textBox.getText().toString();
        //            FlashRunnable flashRunnable = new FlashRunnable(text);
        //            new Thread(flashRunnable).start();
        //        }
        //    }
        //});
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flashMultiplier = 10 - (seekBar.getProgress());

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    class FlashRunnable implements Runnable {
        String text;

        FlashRunnable(String text) {
            this.text = text.toLowerCase();
        }

        @Override
        public void run() {
            for (int i = 0; i < text.length(); i++) {
                char letter = text.charAt(i);

                if (letter == ' ') {
                    waitTimer(175 * flashMultiplier);
                } else {
                    for (int j = 0; j < 39; j++) {
                        if (letter == english[j]) {
                            callFlashLight(j);
                            break;
                        }
                    }
                }
            }
            changePowerButtonSetting();
        }

        private void waitTimer(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
            }
        }

        private void flashLightOn() {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, true);
                flashLightStatus = true;
            } catch (CameraAccessException e) {
            }
        }

        private void flashLightOff() {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, false);
                flashLightStatus = false;
            } catch (CameraAccessException e) {
            }
        }

        private void flashTimer(int time) {
            flashLightOn();
            waitTimer(time);
            flashLightOff();
            waitTimer(25 * flashMultiplier);
        }

        private void callFlashLight(int n) {
            String code = morse[n];
            for (int i = 0; i < code.length(); i++) {
                if (code.charAt(i) == '.') {
                    flashTimer(25 * flashMultiplier);
                } else if (code.charAt(i) == '-') {
                    flashTimer(75 * flashMultiplier);
                } else {
                    Toast.makeText(MainActivity.this, "Error in the code array..",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class SoundRunnable implements Runnable {
        String text;

        SoundRunnable(String text) {
            this.text = text.toLowerCase();
        }

        @Override
        public void run() {

            for (int i = 0; i < text.length(); i++) {
                char letter = text.charAt(i);
                if (letter == ' ') {
                    waitTimer(175 * flashMultiplier);
                } else {
                    for (int j = 0; j < 39; j++) {
                        if (letter == english[j]) {
                            callSound(j);
                            break;
                        }
                    }
                }
            }
            //changePowerButtonSetting();
        }



        private void soundTimer(final int time) {
            SoundPool soundPool = setupSoundPool();
            final int sound1 = soundPool.load(MainActivity.this, R.raw.beep, 1);

            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    soundPool.play(sound1, 0.1f, 0.1f, 0, 0, 1);
                    waitTimer(time);
                    soundPool.stop(sound1);
                    //soundPool.release();
                    waitTimer(25 * flashMultiplier);
                }
            });
        }

        private void waitTimer(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {}
        }

        private void callSound(int n) {
            String code = morse[n];
            for (int i = 0; i < code.length(); i++) {
                if (code.charAt(i) == '.') {
                    soundTimer(25 * flashMultiplier);
                } else
                    soundTimer(75 * flashMultiplier);
            }
        }
        private SoundPool setupSoundPool() {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            SoundPool soundPool;
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
            return soundPool;
        }
    }
}


