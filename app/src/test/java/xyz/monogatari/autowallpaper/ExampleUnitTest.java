package xyz.monogatari.autowallpaper;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@SuppressWarnings("ConstantConditions")
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {

        Boolean boolObj = null;

        if (boolObj == null) {
            System.out.println("null");
        } else {
            System.out.println("not null");
        }

        boolObj = true;
        if ( Boolean.TRUE.equals(boolObj) ) {
            System.out.println("true");
        } else {
            System.out.println("false & null");
        }
        System.out.println("");


        boolObj = false;
        if ( Boolean.TRUE.equals(boolObj) ) {
            System.out.println("true");
        } else {
            System.out.println("false & null");
        }
        System.out.println("");


        boolObj = null;
        if ( Boolean.TRUE.equals(boolObj) ) {
            System.out.println("true");
        } else {
            System.out.println("false & null");
        }
        System.out.println("");

        if ( boolObj ) {
            System.out.println("true");
        } else {
            System.out.println("false & null");
        }
        System.out.println("");





        assertEquals(4, 2 + 2);

    }
}