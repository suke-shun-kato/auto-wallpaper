package xyz.monogatari.suke.autowallpaper.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import static android.graphics.Bitmap.createBitmap;


/**
 * 画像を壁紙ように加工する
 * Created by k-shunsuke on 2017/12/17.
 */

public class BitmapProcessor {
    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    public static final int FIT_WIDTH = 1;
    public static final int FIT_HEIGHT = 2;
    public static final int FIT_BOTH = 3;

    // --------------------------------------------------------------------
    // staticメソッド
    // --------------------------------------------------------------------
    /************************************
     * 指定のサイズへ画像を加工する
     * @param fromBitmap 加工元の画像のBitmap
     * @param toWidth 加工後の画像の幅
     * @param toHeight 加工後の画像の幅
     */
    public static Bitmap process(Bitmap fromBitmap, int toWidth, int toHeight) {
        // ----------------------------------
        // 縦フィット画像か横フィット画像か求める
        // ----------------------------------
        int whichFitXY = BitmapProcessor.whichFitWH(
                fromBitmap.getWidth(), fromBitmap.getHeight(),
                toWidth, toHeight
        );

        // ---------------------------------
        // 変換後の画像のCanvasを作成
        // ---------------------------------
        Bitmap toBitmap = createBitmap(toWidth, toHeight, Bitmap.Config.ARGB_8888);
        Canvas toCanvas = new Canvas(toBitmap);

        // ---------------------------------
        // 配置先の元画像の配置を計算する
        // ---------------------------------
        // ↓これを求める
        RectF toRectF;
        float newFromWidth, newFromHeight;

        switch (whichFitXY) {
            case FIT_WIDTH:
            case FIT_BOTH:
                newFromWidth = (float) toWidth;
                newFromHeight = (float) fromBitmap.getHeight() * toWidth / fromBitmap.getWidth();
                break;
            case FIT_HEIGHT:
                newFromWidth = (float) fromBitmap.getWidth() * toHeight / fromBitmap.getHeight();
                newFromHeight = (float) toHeight;
                break;
            default:
                throw new IllegalStateException("whichFitWH()がおかしいです");
        }

        toRectF = new RectF(
                toWidth/2 - newFromWidth/2, toHeight/2 - newFromHeight/2,
                toWidth/2 + newFromWidth/2, toHeight/2 + newFromHeight/2
        );

        // ---------------------------------
        // 変換
        // ---------------------------------
        toCanvas.drawBitmap(
                fromBitmap,
                new Rect(0, 0, fromBitmap.getWidth(), fromBitmap.getHeight()),
                toRectF,
                new Paint()
        );

        return toBitmap;
    }


    /************************************
     * 縦にフィットさせるか、横にフィットさせるか
     * @param fromW 変更前の画像の幅
     * @param fromH 変更前の画像の高さ
     * @param toW フィットさせる対象の画像の幅
     * @param toH フィットさせる対象の画像の高さ
     * @return 縦にフィットするか、横にフィットするか、等しいか
     */
    private static int whichFitWH(int fromW, int fromH, int toW, int toH) {
        //// とりあえず横サイズ（幅）にフィットさせたときの変更後の縦サイズ（高さ）を計算する
//        double processedFromW = toW;
        double processedFromH = (double)fromH * toW / fromW;

        //// return
        if (processedFromH == toH) {
            return FIT_BOTH;
        } else if (processedFromH < toH) {
            return FIT_WIDTH;
        } else {
            return FIT_HEIGHT;
        }
    }
}
