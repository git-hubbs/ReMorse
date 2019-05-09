//Authors: Jason Hubbs
//Date Started: 04-10-2019

package com.example.remorse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.SoundPool;
import android.app.Dialog;

import java.util.ArrayList;


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


    int flashMultiplier = 5;
    boolean flash = false;
    boolean sound = false;
    String text;
    boolean stopThread = false;
    ImageButton powerButton;
    boolean flashAndSound = false;
    boolean powerIsOn = false;

    TextView progressText;
    ImageButton cancel;

    public void changePowerButtonSetting() {
        powerButton.setImageResource(R.drawable.power);
        powerIsOn = false;
    }
    public void hideProgress() {
        progressText.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        progressText.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText textBox = (EditText) findViewById(R.id.editText);
        final TextView morseView = (TextView) findViewById(R.id.textView3);
        progressText = (TextView) findViewById(R.id.progressText);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final ImageButton menu = (ImageButton) findViewById(R.id.imageButton3);
        final ImageButton lightButton = (ImageButton) findViewById(R.id.lightButton);
        final ImageButton soundButton = (ImageButton) findViewById(R.id.soundButton);
        final TextView flashView = (TextView) findViewById(R.id.flashView);
        final TextView soundView = (TextView) findViewById(R.id.soundView);
        final TextView speedView = (TextView) findViewById(R.id.speedView);
        cancel = (ImageButton) findViewById(R.id.cancel);
        powerButton = (ImageButton) findViewById(R.id.powerButton);



        Typeface atomic = Typeface.createFromAsset(getAssets(), "fonts/andale-mono.ttf");
        Typeface morseFont = Typeface.createFromAsset(getAssets(), "fonts/morse.ttf");
        textBox.setTypeface(atomic);
        morseView.setTypeface(morseFont);

        textBox.requestFocus();




        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!powerIsOn) {
                    stopThread = false;
                    powerIsOn = true;
                    text = textBox.getText().toString().toLowerCase();
                    ArrayList<Integer> waitTimes = new ArrayList<>();
                    for (int i = 0; i < text.length(); i++) {
                        char letter = text.charAt(i);
                        if (letter == ' ') {
                            waitTimes.add(5); //175
                        } else {
                            for (int j = 0; j < 39; j++) {
                                if (letter == english[j]) {
                                    String morseType = morse[j];
                                    for (int k = 0; k < morseType.length(); k++) {
                                        if (morseType.charAt(k) == '.') {
                                            waitTimes.add(1); //25 dot
                                            if (k != morseType.length() - 1)
                                                waitTimes.add(3); //25 wait
                                        } else if (morseType.charAt(k) == '-') {
                                            waitTimes.add(2); //75 dash
                                            if (k != morseType.length() - 1)
                                                waitTimes.add(3); //25 wait
                                        }
                                    }
                                    waitTimes.add(4); //75 wait
                                    break;
                                }
                            }
                        }
                    }
                    if (sound && flash)
                        flashAndSound = true;
                    if (sound) {
                        progressText.setVisibility(View.VISIBLE);
                        cancel.setVisibility(View.VISIBLE);
                        SoundRunnable soundRunnable = new SoundRunnable(waitTimes, text);
                        new Thread(soundRunnable).start();
                        powerButton.setImageResource(R.drawable.poweron);
                    }
                    if (flash) {
                        progressText.setVisibility(View.VISIBLE);
                        cancel.setVisibility(View.VISIBLE);
                        FlashRunnable flashRunnable = new FlashRunnable(waitTimes, text);
                        new Thread(flashRunnable).start();
                        powerButton.setImageResource(R.drawable.poweron);
                    }
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

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stopThread) stopThread = true;
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
                                if (!stopThread) stopThread = true;
                                Intent learnIntent = new Intent(MainActivity.this, Main2Activity.class);
                                MainActivity.this.startActivity(learnIntent);
                                return true;
                            case R.id.ReceiveActivity:
                                if (!stopThread) stopThread = true;
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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flashMultiplier = 11 - (seekBar.getProgress());
                speedView.setText("Speed: " + flashMultiplier * 25 + "ms");
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
        String progressString;
        ArrayList<Integer> waitTimes;
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        FlashRunnable(ArrayList<Integer> waitTimes, String text) {
            this.waitTimes = waitTimes;
            this.text = text;
            this.progressString = "";
        }

        @Override
        public void run() {
            int textItr = 0;
            progressString += text.charAt(textItr);
            progressText.setText(progressString);
            textItr += 1;
            for (int i = 0; i < waitTimes.size(); i++) {
                int action = waitTimes.get(i);
                if(stopThread)
                    break;
                if (action == 1) {
                    flashFor(25);
                } else if (action == 2) {
                    flashFor(75);
                } else if (action == 3) {
                    waitTimer(25);
                } else if (action == 4) {
                    waitTimer(75);
                    if (textItr < text.length()) {
                        progressString += text.charAt(textItr);
                        progressText.setText(progressString);
                        textItr += 1;
                    }
                } else { //action == 5
                    waitTimer(100);
                    if (textItr < text.length()) {
                        progressString += text.charAt(textItr);
                        progressText.setText(progressString);
                        textItr += 1;
                    }
                }
            }
            changePowerButtonSetting();
            hideProgress();
        }

        private void flashFor(int time) {
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, true);
                flashLightStatus = true;
            } catch (CameraAccessException e) { e.printStackTrace(); }
            try {
                Thread.sleep(time * flashMultiplier);
            } catch (InterruptedException e) { e.printStackTrace(); }
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, false);
                flashLightStatus = false;
            } catch (CameraAccessException e) { e.printStackTrace(); }
        }

        private void waitTimer(int time) {
            try {
                Thread.sleep(time * flashMultiplier);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    class SoundRunnable implements Runnable {
        String text;
        String progressString;
        ArrayList<Integer> waitTimes;
        ToneGenerator beep = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);

        SoundRunnable(ArrayList<Integer> waitTimes, String text) {
            this.waitTimes = waitTimes;
            this.text = text;
            this.progressString = "";
        }

        @Override
        public void run() {
            int textItr = 0;
            if (!flashAndSound) {
                progressString += text.charAt(textItr);
                progressText.setText(progressString);
                textItr += 1;
            }
            for (int i = 0; i < waitTimes.size(); i++) {
                int action = waitTimes.get(i);
                if(stopThread)
                    break;
                if (action == 1) {
                    playBeep(25);
                } else if (action == 2) {
                    playBeep(75);
                } else if (action == 3) {
                    waitTimer(25);
                } else if (action == 4) {
                    waitTimer(75);
                    if (!flashAndSound) {
                        if (textItr < text.length()) {
                            progressString += text.charAt(textItr);
                            progressText.setText(progressString);
                            textItr += 1;
                        }
                    }
                } else { //action == 5
                    waitTimer(100);
                    if (!flashAndSound) {
                        if (textItr < text.length()) {
                            progressString += text.charAt(textItr);
                            progressText.setText(progressString);
                            textItr += 1;
                        }
                    }
                }
            }
            if (!flashAndSound) {
                changePowerButtonSetting();
                hideProgress();
            } else
                flashAndSound = false;


        }

        private void playBeep(int time) {
            beep.startTone(ToneGenerator.TONE_DTMF_7);
            try {
                Thread.sleep(time * flashMultiplier);
            } catch (InterruptedException e) { e.printStackTrace(); }
            beep.stopTone();
        }

        private void waitTimer(int time) {
            try {
                Thread.sleep(time * flashMultiplier);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
}