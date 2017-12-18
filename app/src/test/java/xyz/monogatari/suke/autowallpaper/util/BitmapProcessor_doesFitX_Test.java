package xyz.monogatari.suke.autowallpaper.util;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * テスト
 * Created by k-shunsuke on 2017/12/17.
 */
public class BitmapProcessor_doesFitX_Test {
    private Method method;

    @Before
    public void before() throws Exception {
        this.method = BitmapProcessor.class.getDeclaredMethod(
                "whichFitXY", int.class, int.class, int.class, int.class);
        this.method.setAccessible(true);
    }

    @Test
    public void equal() throws Exception {
        assertEquals(
                BitmapProcessor.FIT_BOTH,
                this.method.invoke(null, 1,2,1,2)
        );

        assertEquals(
                BitmapProcessor.FIT_BOTH,
                this.method.invoke(null, 200,124,100,62)
     );

        // 小数点切り捨てのときはエラー
        assertNotEquals(
                BitmapProcessor.FIT_BOTH,
                this.method.invoke(null, 9999,100,3333,33)

        );

    }

    @Test
    public void fitX() throws Exception {
        assertEquals(
                BitmapProcessor.FIT_WIDTH,
                this.method.invoke(null, 100, 200, 50, 121)
        );
        assertEquals(
                BitmapProcessor.FIT_WIDTH,
                this.method.invoke(null, 100, 200, 99, 199)
        );
    }

    @Test
    public void fitY() throws Exception {
        assertEquals(
                BitmapProcessor.FIT_HEIGHT,
                this.method.invoke(null, 100, 200, 50, 98)
        );
        assertEquals(
                BitmapProcessor.FIT_HEIGHT,
                this.method.invoke(null, 100, 200, 99, 101)
        );
    }
}
