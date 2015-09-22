package se.chalmers.student.eyetracker;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.CameraBridgeViewBase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



/**
 * Created by Sebka on 10/10/14.
 */
public class CameraActivity extends Activity {
    private static final String TAG = "EyeTrackerActivity";
    public static final int JAVA_DETECTOR = 0;

    private File mCascadeDir;
    private Camera eyeTracker;
    private SignalTimer signalTimer;
    private CombineSignals combineSignals;
    private MediaPlayer mp;
    private ToXml toXml;

    private Button btn;
    private TextView alarmView;
    private CountDownTimer timer;

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
        toXml = new ToXml();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        CameraBridgeViewBase cameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        signalTimer = new SignalTimer(this);
        signalTimer.start();
        combineSignals = new CombineSignals(signalTimer);

        alarmView = (TextView) findViewById(R.id.alarmTime);
        alarmView.setVisibility(View.INVISIBLE);
        btn = (Button) findViewById(R.id.btnViewOn);


        File cascadeFile;
        File cascadeFileER;

        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(
                    R.raw.lbpcascade_frontalface);
            mCascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            cascadeFile = new File(mCascadeDir,
                    "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // --------------------------------- load left eye
            // classificator -----------------------------------
            InputStream iser = getResources().openRawResource(
                    R.raw.haarcascade_lefteye_2splits);
            File cascadeDirER = getDir("cascadeER",
                    Context.MODE_PRIVATE);
            cascadeFileER = new File(cascadeDirER,
                    "haarcascade_eye_right.xml");
            FileOutputStream oser = new FileOutputStream(cascadeFileER);

            byte[] bufferER = new byte[4096];
            int bytesReadER;
            while ((bytesReadER = iser.read(bufferER)) != -1) {
                oser.write(bufferER, 0, bytesReadER);
            }
            iser.close();
            oser.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
            throw new RuntimeException();
        }
        eyeTracker = new Camera(cameraView, cascadeFile, cascadeFileER, this, signalTimer);
    }

    public void viewOn(View view){
        double percentage = (double)signalTimer.getTimeAway() / (double)signalTimer.getTotalDriveTime() * 100.0;

        alarmView.setVisibility(View.VISIBLE);
        alarmView.setText(String.format("%d Alarms\n %.1f%% Off Time", signalTimer.getNumberOfAlarms(), percentage));
        //alarmAway.setVisibility(View.VISIBLE);
        //alarmAway.setText("" + signalTimer.getTimeAway()/signalTimer.getTotalDriveTime() + "%");
        btn.setVisibility(View.INVISIBLE);
        timer = new CountDownTimer(5000, 1000) {
            int sec = 0;

            @Override
            public void onTick(long l) {
                double percentage = (double)signalTimer.getTimeAway() / (double)signalTimer.getTotalDriveTime() * 100.0;

                alarmView.setText(String.format("%d Alarms\n %.1f%% Off Time", signalTimer.getNumberOfAlarms(), percentage));  // ifall den går igång under tiden den visas.
            }

            @Override
            public void onFinish() {
                alarmView.setVisibility(View.INVISIBLE);
                //alarmAway.setVisibility(View.INVISIBLE);
                btn.setVisibility(View.VISIBLE);
            }
        };
        timer.start();
    }

    @Override
    public void onPause() {
        moveTaskToBack(true);
        super.onPause();
        eyeTracker.pause();
        signalTimer.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        eyeTracker.pause();
        signalTimer.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        eyeTracker.resume(this);
        signalTimer.onResume();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        eyeTracker.resume(this);
        signalTimer.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        eyeTracker.destroy();
        mCascadeDir.delete();
        signalTimer.onPause();

    }

    /*public void showAlarm(){

        tvAlarm.setText("This is a alarm !");
    }*/

    public void playAlarm() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mp = MediaPlayer.create(getApplicationContext(), notification);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

   /* public void saveToXml(){
        toXml.save("fileToXML.txt", this);
    }*/

}