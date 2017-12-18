package xyz.monogatari.suke.autowallpaper.util;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * テスト
 * Created by k-shunsuke on 2017/12/18.
 */
public class BitmapProcessor_calcArea_Test {
    private Method method;

    // private staticのときのテスト
    @Before
    public void before() throws Exception {
        this.method = BitmapProcessor.class.getDeclaredMethod(
                "calcArea", int.class, int.class, int.class, int.class);
        this.method.setAccessible(true);
    }

    /************************************
     * 縦横どちらもフィットしているとき
     */
    @Test
    public void whenFitWAndH() throws Exception {
        assertEquals(
                10.0, //面積
                this.method.invoke(null, 2,5,2,5)
        );

        // 大きくなる時
        assertEquals(
                (double) 8*20, //面積
                this.method.invoke(null, 2,5,8,20)
        );

        // 小さくなるとき
        assertEquals(
                (double) 11*23, //面積
                this.method.invoke(null, 55,115,11,23)
        );
    }

    /************************************
     * 横にフィットしているとき
     */
    @Test
    public void whenFitW() throws Exception {
        // 変わらない時
        assertEquals(
                50.0*33.0, //面積
                this.method.invoke(null, 50,33,50,100)
        );
        // 小さくなるとき
        assertEquals(
                30.0/3 *33.0/3, //面積
                this.method.invoke(null, 30,33,10,23)
        );
        // 大きくなるとき
        assertEquals(
                12.0*10 * 45.0*10, //面積
                this.method.invoke(null, 12,45,120,451)
        );
        // 失敗
        assertNotEquals(
                12.0*10 * 45.0*10, //面積
                this.method.invoke(null, 12,45,120,449)
        );
    }

    /************************************
     * 縦にフィットしているとき
     */
    @Test
    public void whenFitH() throws Exception {
        // 変わらない時
        assertEquals(
                50.0*33.0, //面積
                this.method.invoke(null, 50,33,51,33)
        );
        // 小さくなるとき
        assertEquals(
                50.0/5 *55.0/5, //面積
                this.method.invoke(null, 50,55,11,11)
        );
        // 大きくなるとき
        assertEquals(
                12.0*10 * 45.0*10, //面積
                this.method.invoke(null, 12,45,123,450)
        );
        // 失敗
        assertNotEquals(
                12.0*10 * 45.0*10, //面積
                this.method.invoke(null, 12,45,119,450)
        );
    }
}
