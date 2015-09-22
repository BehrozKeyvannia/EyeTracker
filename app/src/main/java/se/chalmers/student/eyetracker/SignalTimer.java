package se.chalmers.student.eyetracker;

import  java.util.Observable;
import java.util.Observer;

/**
 * Created by Akar on 2014-10-12.
 */

public class SignalTimer extends Thread implements Observer{
    private boolean reset = false;
    private int disLevel = 0;
    private int time = 5000;
    private int diff = 0;
    private CameraActivity cameraActivity;
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mFinished;
    private int totalDriveTime=0;
    private int timeAway=0;
    private int tempTime=0;
    private int numberOfAlarms = 0;
    private boolean alarmGiven = true;


    public SignalTimer(CameraActivity cameraActivity){
        this.cameraActivity = cameraActivity;
        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;
    }

    @Override
    public void run(){
        while ( !mFinished ) {
            //Log.v("In i while true", timerTag);
            if (time > 0) {
                //Log.v("In i IF satsen time > 0", timerTag);
                alarmGiven = true;
                if (reset == true) {
                    resetTime();
                }
                else if (disLevel == 9) { //Stantstill
                    resetTime();
                }
                else if (disLevel == -1) {    //Error
                    try {
                        throw new InvalidSpeedException("Invalid speed");
                    } catch (InvalidSpeedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //Log.v("In i else satsen disLevel > 0", timerTag);


                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tempTime += 10;
                    if(tempTime >= 100){
                        timeAway += tempTime;
                        tempTime = 0;
                    }
                    time -= 10;
                    totalDriveTime += 10;

                }
            } else {
                if(alarmGiven){
                    numberOfAlarms += 1;
                    alarmGiven = false;
                }

                if (disLevel == 9) { //Stantstill
                    resetTime();
                }
                //Log.v("In i else satsen", timerTag);
                try {
                    cameraActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cameraActivity.playAlarm();
                        }
                    });
                    sleep(1000);
                    timeAway += 1000;
                    totalDriveTime += 1000;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //cameraActivity.saveToXml();
            }
            synchronized (mPauseLock) {
                while (mPaused) {
                    try {
                        mPauseLock.wait();
                    } catch (InterruptedException e) {}
                }
            }
        }
    }


    public void resetTime(){
        time = 1000 +( disLevel * 400 );
    }

    public void incTime(int diff){
        time += diff * 400;
    }

    public void decTime(int diff){
        time -= diff * 400;
    }

    public int getTotalDriveTime(){
        return totalDriveTime;
    }

    public int getTimeAway(){
        return timeAway;
    }

    public int getNumberOfAlarms(){
        return numberOfAlarms;
    }

    @Override
    public void update(Observable obs, Object obj) {
        // TODO Auto-generated method stub
        if(obs instanceof Camera && obj instanceof Boolean){
            reset = (Boolean) obj;
            if(reset){
                resetTime();
                tempTime = 0;
            }
        }

        else if( obs instanceof CombineSignals && obj instanceof Integer ){
            int received = (Integer) obj;

            if( disLevel < received ){
                diff = received - disLevel;
                disLevel = received;
                decTime(diff);
            }
            else if( disLevel > received ){
                diff = disLevel - received;
                disLevel = received;
                incTime(diff);
            }
        }
    }

    public void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }
    public void onResume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }
}
