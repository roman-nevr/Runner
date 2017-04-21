package org.berendeev.roma.runner;

import android.content.Context;

import org.berendeev.roma.runner.data.LocationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest= Config.NONE)
public class GpsTest {

    Context context;

    @Before
    public void before(){
        context = RuntimeEnvironment.application.getApplicationContext();
    }

    @Test
    public void gps(){
        LocationRepository locationRepository = new LocationRepository(context);
        System.out.println(locationRepository.isGpsEnabled());
    }
}
