package se.chalmers.student.eyetracker;

import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSFloat;
import android.swedspot.scs.data.SCSInteger;
import android.swedspot.scs.data.SCSShort;
import android.util.Log;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;
import java.lang.Runnable;
import java.util.Observable;

/**
 * Created by Soroush on 09/10/14.
 * Purpose: Combine two signals
 * The class has a method that called combine. This method return an integer depends on
 * speed and distractionLevel.
 * Created by Soroush on 15/10/14.
 * Added Signal for rear gear
 *
 */
public class CombineSignals extends Observable {

    private float speed = 1;
    private int distractionLevel = 1;
    private short selectedGear = 0;
    private final String TAG = "TAG";

    public CombineSignals(SignalTimer signalTimer) {
        addObserver(signalTimer);
        receiveSignal();
    }

    public void combine(float combination){

        if ( combination == 0 ){
            //Log.v(TAG, "1");
            setChanged();
            notifyObservers(9); //The truck is stopped or in rear gear
        }
        else if ( combination > 0 && combination <= 20 ){
            //Log.v(TAG, "2");
            setChanged();
            notifyObservers(8);   //Lowest priority alarm signal
        }
        else if ( combination > 20 && combination <= 40){
            //Log.v(TAG, "3");
            setChanged();
            notifyObservers(7);
        }
        else if ( combination > 40 && combination <= 70){
            //Log.v(TAG, "4");
            setChanged();
            notifyObservers(6);
        }
        else if ( combination > 70 && combination <= 100){
            //Log.v(TAG, "5");
            setChanged();
            notifyObservers(5);
        }
        else if ( combination > 100 && combination <= 140){
            //Log.v(TAG, "6");
            setChanged();
            notifyObservers(4);
        }
        else if ( combination > 140 && combination <= 210){
            //Log.v(TAG, "7");
            setChanged();
            notifyObservers(3);
        }
        else if ( combination > 210 && combination <= 320){
            //Log.v(TAG, "8");
            setChanged();
            notifyObservers(2);
        }
        else if ( combination > 320 ){
            //Log.v(TAG, "9");
            /*tv.post(new Runnable() {
                @Override
                public void run() {
                    tv.setText("9");
                }
            });*/
            setChanged();
            notifyObservers(1);   //Highest priority alarm signal
        }
        else {
            setChanged();
            notifyObservers(-1);  //Error
        }
    }

    private void receiveSignal() {

        new Thread( new Runnable() {

            @Override
            public void run() {
                AutomotiveFactory.createAutomotiveManagerInstance(
                        new AutomotiveCertificate(new byte[0]),
                        new AutomotiveListener() { // Listener that observes the Signals
                            @Override
                            public void receive(final AutomotiveSignal automotiveSignal) {
                                if (automotiveSignal.getSignalId() == AutomotiveSignalId.FMS_WHEEL_BASED_SPEED) {
                                    speed = (((SCSFloat) automotiveSignal.getData()).getFloatValue());
                                    combine(speed * distractionLevel);
                                }
                                else if (automotiveSignal.getSignalId() == AutomotiveSignalId.FMS_SELECTED_GEAR){
                                    selectedGear = (((SCSShort) automotiveSignal.getData()).getShortValue());
                                    if(selectedGear == -1 || selectedGear == -2){
                                        combine(0);
                                    }
                                    else {
                                        combine(speed * distractionLevel);
                                    }
                                }
                            }

                            @Override
                            public void timeout(int i) {
                            }

                            @Override
                            public void notAllowed(int i) {
                            }
                        },
                        new DriverDistractionListener() {       // Observe driver distraction level
                            @Override
                            public void levelChanged(final DriverDistractionLevel driverDistractionLevel) {
                                distractionLevel = driverDistractionLevel.getLevel();
                                combine(speed * distractionLevel);
                            }
                        }
                ).register(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, AutomotiveSignalId.FMS_SELECTED_GEAR);
            }
        }).start();
    }
}
