package se.chalmers.student.eyetracker;

/**
 * Created by Soroush on 12/10/14.
 * Purpose: Check engine is running and distractionLevel is not high. Then allow user start the app.
 * Next (not implemented): The doors is closed. [how register two signal ? ]
 *
 *
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSFloat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;


public class StartPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        final TextView tvOffOrOn = (TextView) findViewById(R.id.tvOffOrOn);
        final TextView tvDistraction = (TextView) findViewById(R.id.tvDistraction);
        final Button button = (Button) findViewById(R.id.startButton);
        final Intent myIntent = new Intent(this, CameraActivity.class);

        new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object... objects) {
                // Access to Automotive API
                AutomotiveFactory.createAutomotiveManagerInstance(
                        new AutomotiveCertificate(new byte[0]),
                        new AutomotiveListener() { // Listener that observes the Signals
                            @Override
                            public void receive(final AutomotiveSignal automotiveSignal) {
                                tvOffOrOn.post(new Runnable() { // Post the result back to the View/UI thread
                                    public void run() {
                                        if ((((SCSFloat) automotiveSignal.getData()).getFloatValue()) > 0) {
                                            tvOffOrOn.setText("The Truck is on");
                                            tvOffOrOn.setTextColor(Color.GREEN);
                                            button.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    startActivity(myIntent);
                                                }
                                            });
                                        } else {
                                            tvOffOrOn.setText("The Truck is off");
                                            tvOffOrOn.setTextColor(Color.RED);
                                            button.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    AlertDialog alertDialog = new AlertDialog.Builder(StartPage.this).create();
                                                    alertDialog.setTitle("Engine");
                                                    alertDialog.setMessage("The truck is off ! ");
                                                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // TODO Add your code for the button here.
                                                        }
                                                    });// Set the Icon for the Dialog
                                                    alertDialog.setIcon(R.drawable.ic_alert);
                                                    alertDialog.show();
                                                }
                                            });
                                        }
                                    }
                                });
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
                                tvDistraction.post(new Runnable() { // Post the result back to the View/UI thread
                                    public void run() {

                                        switch (driverDistractionLevel.getLevel()) {
                                            case 1:
                                                tvDistraction.setText("LOW");
                                                tvDistraction.setTextColor(Color.GREEN);
                                                button.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        startActivity(myIntent);
                                                    }
                                                });
                                                break;
                                            case 2:
                                                tvDistraction.setText("INTERMEDIATE");
                                                tvDistraction.setTextColor(Color.YELLOW);
                                                button.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        startActivity(myIntent);
                                                    }
                                                });
                                                break;
                                            case 3:
                                                tvDistraction.setText("HIGH");
                                                tvDistraction.setTextColor(Color.rgb(255, 153, 0));
                                                button.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        startActivity(myIntent);
                                                    }
                                                });
                                                break;
                                            case 4:
                                                tvDistraction.setText("VERY HIGH");
                                                tvDistraction.setTextColor(Color.RED);
                                                button.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                switch (which) {
                                                                    case DialogInterface.BUTTON_POSITIVE:
                                                                        startActivity(myIntent);
                                                                        break;
                                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                                        //No button clicked
                                                                        break;
                                                                }
                                                            }
                                                        };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(StartPage.this);
                                                        builder.setMessage("You are very distracted. Do you want to drive?").setPositiveButton("Yes", dialogClickListener)
                                                                .setNegativeButton("No", dialogClickListener).show();
                                                    }
                                                });
                                                break;
                                            default:
                                                tvDistraction.setText("Standstill");
                                                tvDistraction.setTextColor(Color.WHITE);
                                                button.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        startActivity(myIntent);
                                                    }
                                                });
                                                break;
                                        }
                                    }
                                });
                            }
                        }
                ).register(AutomotiveSignalId.FMS_ENGINE_SPEED); // Register for the engine signal
                return null;
            }
        }.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.first, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}