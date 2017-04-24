package org.berendeev.roma.runner;

import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void stringFormat(){
        String string = (String.format(Locale.getDefault(), "time: %1$tF %1$tT", new Date(System.currentTimeMillis())));
        System.out.println(string);
    }
}