package xyz.monogatari.autowallpaper.service;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * MainService.calcDelayMsec()のテストコード
 * Created by k-shunsuke on 2018/01/02.
 */
public class MainService_calcStartUnixTime {
    private Method method;
    private final double COMPRESS_MSEC_500 = 500.0;

    // private staticのときのテスト
    @Before
    public void before() throws Exception {
        // calcStartUnixTime(long intervalMsec, double mag, long nowUnixTimeMsec)
        this.method = MainService.class.getDeclaredMethod(
                "calcStartUnixTime", long.class, double.class, long.class);
        this.method.setAccessible(true);
    }

    @Test
    public void normal() throws Exception {
        assertEquals(
                5000*0.5+1000000,   // 期待値
                this.method.invoke(null, 5000, 0.5, 1000000)   // static なのでnull
        );

        long now = System.currentTimeMillis();
        assertEquals(
                2400000*0.7+now,   // 期待値
                this.method.invoke(null, 2400000, 0.7, now)   // static なのでnull
        );

    }
    @Test
    public void lowCompress() throws Exception {
        long now = System.currentTimeMillis();
        assertEquals(
                COMPRESS_MSEC_500 + now,   // 期待値
                this.method.invoke(null, 5000, 0.0, now)   // static なのでnull
        );

        assertEquals(
                COMPRESS_MSEC_500 + 121323123,   // 期待値
                this.method.invoke(null, 1000000000, 0.0, 121323123)
        );
        assertEquals(
                COMPRESS_MSEC_500 + now,   // 期待値
                this.method.invoke(null, 5000, 0.1, now)
        );
        assertEquals(
                5000*0.1 + now,   // 期待値
                this.method.invoke(null, 5000, 0.1, now)
        );
        assertNotEquals(
                COMPRESS_MSEC_500 + 121323123,   // 期待値
                this.method.invoke(null, 5001, 0.1, 121323123)
        );
    }
    @Test
    public void heightCompress() throws Exception {
        assertEquals(5000 - COMPRESS_MSEC_500 + 12358761,
                this.method.invoke(null, 5000, 1.0, 12358761)
        );
        assertEquals(10000 - COMPRESS_MSEC_500 + 4444,
                this.method.invoke(null, 10000, 0.96, 4444)
        );
        assertEquals(10000 - COMPRESS_MSEC_500 + 465,
                this.method.invoke(null, 10000, 0.95, 465)
        );
        assertNotEquals(10000 - COMPRESS_MSEC_500 + 465,
                this.method.invoke(null, 10000, 0.94, 465)
        );
        assertEquals(10000 * 0.94 + 0,
                this.method.invoke(null, 10000, 0.94, 0)
        );
    }
    @Test(expected = InvocationTargetException.class)
    public void throwError() throws Exception {
      this.method.invoke(null, 500, 1.0, 12358761);
    }
    @Test(expected = InvocationTargetException.class)
    public void throwError2() throws Exception {
      this.method.invoke(null, 999, 2, 12358761);
    }
    @Test
    public void notThrowError() throws Exception {
      this.method.invoke(null, 1000, 2, 12358761);
    }
}


