package xyz.monogatari.suke.autowallpaper.service;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

/**
 * Created by k-shunsuke on 2018/01/02.
 */

public class MainService_calcDelayMsec {
    private Method method;

    // private staticのときのテスト
    @Before
    public void before() throws Exception {
        this.method = MainService.class.getDeclaredMethod(
                "calcDelayMsec", long.class, long.class, long.class);
        this.method.setAccessible(true);
    }

    @Test
    public void normal() throws Exception {
        assertEquals(
                1L, //面積
                this.method.invoke(null, 10, 2, 31)
        );
        assertEquals(
                19L, //面積
                this.method.invoke(null, 1000, 20, 1001)
        );
        assertEquals(
                15L, //面積
                this.method.invoke(null, 1000, 20, 2025)
        );
        assertEquals(
                1L, //面積
                this.method.invoke(null, 1000, 50, 2049)
        );
    }

    @Test
    public void kyoukaichi() throws Exception {
        assertEquals(
                0L, //面積
                this.method.invoke(null, 30000000, 5000, 30000000)
        );
        assertEquals(
                0L, //面積
                this.method.invoke(null, 30000000, 5000, 30005000)
        );

    }
    @Test
    public void mainus() throws Exception {
        assertEquals(
                0L, //面積
                this.method.invoke(null, 100, 5, 95)
        );
        assertEquals(
                4L, //面積
                this.method.invoke(null, 100, 5, 96)
        );
        assertEquals(
                4999L, //面積
                this.method.invoke(null, 10000, 5000, 1)
        );

    }
}


