package xyz.monogatari.suke.autowallpaper.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * 画面のサイズを取得したりするクラス
 * 下記のサイトからコードいただきました
 * https://qiita.com/a_nishimura/items/f557138b2d67b9e1877c
 * Created by k-shunsuke on 2017/12/17.
 */

@SuppressWarnings("WeakerAccess")
public class DisplaySizeCheck {

    /**
     * Get a Display Size
     * @param context 現在のコンテキスト
     * @return Point, Point.x or Point.y
     */
    public static Point getDisplaySize(Context context){
        //noinspection ConstantConditions
        @SuppressWarnings("ConstantConditions") Display display = ((WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }

    /**
     * Get a Real Size(Hardware Size)
     * @param context 現在のコンテキスト
     * @return Pointオブジェクト
     */
    @SuppressLint("NewApi")
    public static Point getRealSize(Context context) {

        @SuppressWarnings("ConstantConditions") Display display = ((WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        Point point = new Point(0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Android 4.2~
            display.getRealSize(point);
            return point;

        } else {
            // Android 4.1のみ
            try {
                Method getRawWidth = Display.class.getMethod("getRawWidth");
                Method getRawHeight = Display.class.getMethod("getRawHeight");
                int width = (Integer) getRawWidth.invoke(display);
                int height = (Integer) getRawHeight.invoke(display);
                point.set(width, height);
                return point;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return point;
    }

    /**
     * Get a view size. if display view size, after onWindowFocusChanged of method
     * @param View 取得対象のView
     * @return Pointオブジェクト
     */
    public static Point getViewSize(View View){
        Point point = new Point(0, 0);
        point.set(View.getWidth(), View.getHeight());

        return point;
    }

}
