package com.example.remorse;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.os.Parcelable;

import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Main3Activity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    Rect rectangle;

    JavaCameraView javaCameraView;
    TextView value;
    TextView morseString;
    TextView type;
    TextView dashSpeed;
    TextView shutterView;
    SeekBar dashSpeedBar;
    SeekBar shutterSpeedBar;
    double lumonosity;
    double previousLumonosity;
    int counter;
    int rows;
    int cols;
    int shutterMultiplier;
    long dashLength = 700;
    long dotLength = dashLength/3;
    long spaceLength = dotLength;
    long lightLength;
    String morse = "";
    long firstTime = 0;
    boolean lightOn = false;
    boolean started = false;
    boolean lightOnStarted = false;
    boolean lightOffStarted = false;
    boolean firstLengthOn = false;
    boolean firstMorse = false;
    long calcDotFirst = 0;
    long firstTimeOn = 0;
    long firstLength = 0;
    long firstTimeOff = 0;




    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:{
                    javaCameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };
    Mat mRgba;

    static{ System.loadLibrary("opencv_java3"); }

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("My App", "Unable to load OpenCV");
        } else {
            Log.d("My App", "OpenCV loaded");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        rectangle = new Rect();

        value = (TextView) findViewById(R.id.valueView);
        morseString = (TextView) findViewById(R.id.fpsView);
        dashSpeedBar = (SeekBar) findViewById(R.id.dashSpeedBar);
        dashSpeed = (TextView) findViewById(R.id.speedView);
        shutterView = (TextView) findViewById(R.id.lightConstraintView);
        shutterSpeedBar = (SeekBar) findViewById(R.id.lightConstraintBar);


        javaCameraView = (JavaCameraView) findViewById(R.id.cameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setMaxFrameSize(288, 592);

        dashSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                dashLength = (seekBar.getProgress());
                dashSpeed.setText("Speed: " + (dashLength*100) + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        shutterSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                shutterMultiplier = seekBar.getProgress();
                shutterView.setText("Shutter: " + shutterMultiplier + "/255");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba = inputFrame.rgba();
        Mat mRbgaT = mRgba.t();
        Core.flip(mRbgaT, mRbgaT, 1);
        Imgproc.resize(mRbgaT, mRbgaT, mRgba.size());

        counter = 0;
        previousLumonosity = lumonosity;
        double R=0,G=0,B=0;


        rows = mRbgaT.rows();
        cols = mRbgaT.cols();

        int beginningRows = (rows/5)*2;
        int endingRows = (rows/5)*3;

        int beginningCols = (cols/5)*2;
        int endingCols = (cols/5)*3;

        rectangle.width = endingCols - beginningCols;
        rectangle.height = endingRows - beginningRows;

        rectangle = new Rect(beginningCols,beginningRows,rectangle.width,rectangle.height);
        Imgproc.rectangle(mRbgaT, rectangle.tl(), rectangle.br(), new Scalar(255, 0, 0), 2, 8, 0);

        for (int i=beginningRows; i<endingRows; i++)
        {
            for (int j=beginningCols; j<endingCols; j++)
            {
                double[] data = mRgba.get(i, j); //Stores element in an array
                R = data[0];
                G = data[1];
                B = data[2];

                if(R > shutterMultiplier){
                    R = 255;
                }else{
                    R = 0;
                }
                if(G > shutterMultiplier){
                    G = 255;
                }else{
                    G = 0;
                }
                if(B > shutterMultiplier){
                    B = 255;
                }else{
                    B = 0;
                }
                lumonosity += (0.3*R + 0.6*G + 0.1*B);
                counter++;
            }
        }

        lumonosity /= counter;
        if(lumonosity > previousLumonosity*2){
            lumonosity = previousLumonosity;
            if (!lightOn && started) {
                lightOnStarted = true;
                dotLength = System.currentTimeMillis() - calcDotFirst;
                dashLength = dotLength * 3;
            }


            if (!lightOn && !started) {
                firstLengthOn = true;
                firstTimeOn = System.currentTimeMillis();
            }

            lightOn = true;
            started = true;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Stuff that updates the UI
                    value.setText("Light detected");
                }
            });
        }
        else{
            if (lightOn) {
                if (firstLengthOn) {
                    firstLength = System.currentTimeMillis() - firstTimeOn;
                    firstLengthOn = false;
                    firstMorse = true;
                }
                lightOffStarted = true;
                calcDotFirst = System.currentTimeMillis();
            }

            lightOn = false;
//            lightOn = true;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    // Stuff that updates the UI
//                    value.setText("Light detected");
//                }
//            });
//        }
//        else{
//            lightOn = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Stuff that updates the UI
                    value.setText("Light NOT detected..");
                }
            });
        }

            //Algorithm for space and light detection
            if(lightOn){
                if(lightOnStarted) {
                    firstTimeOn = System.currentTimeMillis();
                    lightOnStarted = false;
                }
            }
            else{
                if(lightOffStarted) {
                    firstTimeOff = System.currentTimeMillis();
                    lightOffStarted = false;
                    lightLength = firstTimeOff - firstTimeOn;
                    if (firstMorse) {
                        if ((firstLength > dotLength * 0.5) && (firstLength < dotLength * 1.5)) {
                            morse += '.';
                        }
                        if ((firstLength > dashLength * 0.5) && (firstLength < dashLength * 1.5)) {
                            morse += '-';
                        }
                        firstMorse = false;
                    }
                    if ((lightLength > dotLength * 0.5) && (lightLength < dotLength * 1.5)) {
                        morse += '.';
                    }
                    if ((lightLength > dashLength * 0.5) && (lightLength < dashLength * 1.5)) {
                        morse += '-';
                    }
                }
            }
//
//        //Algorithm for space and light detection
//        if(lightOn){
//            if(lightLength == 0) {
//                firstTime = System.currentTimeMillis();
//                lightLength = 1;
//            }
//            else{
//                lightLength = System.currentTimeMillis() - firstTime;
//            }
//        }
//        else{
//            if ((lightLength < dotLength * 1.5) && (lightLength > dotLength * 0.5)) {
//                morse += '.';
//            }
//            if ((lightLength < dashLength * 1.5) && (lightLength > dashLength * 0.5)) {
//                morse += '-';
//            }
//            lightLength = 0;
//        }

        //Update UI with morse text
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                morseString.setText(morse);
            }
        });
        return mRbgaT;
    }

    @Override
    public void onCameraViewStopped(){
        mRgba.release();

    }

    @Override
    public void onCameraViewStarted(int width, int height){
        mRgba = new Mat(height, width, CvType.CV_8UC4);


    }

    @Override
    protected void onPause(){
        super.onPause();
        if(javaCameraView != null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("My App", "Unable to load OpenCV");
            //Make sure to check out newer OpenCV Versions*
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, baseLoaderCallback);
        } else {
            Log.d("My App", "OpenCV loaded");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(javaCameraView != null){
            javaCameraView.disableView();
        }
    }
}