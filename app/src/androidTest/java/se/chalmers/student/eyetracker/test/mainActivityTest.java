package se.chalmers.student.eyetracker.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import se.chalmers.student.eyetracker.R;
import se.chalmers.student.eyetracker.StartPage;

/**
 * Created by Soroush on 13/10/14.
 */
public class mainActivityTest extends ActivityInstrumentationTestCase2<StartPage> {

    StartPage activity;

    public mainActivityTest() {
        super(StartPage.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    public void testTextViewNotNull() throws Exception {

        TextView tv = (TextView) activity.findViewById(R.id.tvInfo);
        assertNotNull(tv);

    }
}
