/*
■XMLの設定の仕方
<xyz.monogatari.autowallpaper.TimePreference
    android:key="aaaaaaaaaa"           // key名
    android:title="タイトルですよ"     // タイトルの文字列
    android:defaultValue="11:11:11"    // デフォルト値　
    />
android:summary は設定しない。設定された値が表示されるので

■SPに保存される値
 元期からの UTC（協定世界時、世界標準時間）をミリ秒値で表される時間 long型

■SPに値が保存されていないときは↓の関数を呼ぶ
 TimePreference.getDefaultSpValue()
*/

package xyz.monogatari.autowallpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/************************************
 * (参考URL)↓
 * https://stackoverflow.com/questions/5533078/timepicker-in-preferencescreen/34398747
 *
 * 公式のカスタムPreferenceの作成の方法マニュアル
 * https://developer.android.com/guide/topics/ui/settings?hl=ja#Custom
 *
 */
public class TimePreference extends DialogPreference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    // 内部的な設定の値、SPから読み込んだ値
    private Calendar calendar;
    // ビュー
    private TimePicker picker = null;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
Log.d("○○"+getClass().getSimpleName(),  "コンストラクタ、supre後()");


        //// 時刻ダイアログの設定をする

        // ↓は不必要自分が作成したダイアログのレイアウトXMLを使うわけじゃないので不必要、
        // 代わりにonCreateDialogView()で既存のダイアログを生成している
        // setDialogLayoutResource(R.layout.xxxx );

        this.setPositiveButtonText(android.R.string.ok);
        this.setNegativeButtonText(android.R.string.cancel);
        this.setDialogIcon(null);

        //// 時刻ダイアログに使うためのインスタンスを生成
        // デフォルトロケールでデフォルトタイムゾーンの現在の時間に基づいてデフォルトの GregorianCalendar を構築します。
        this.calendar = new GregorianCalendar();

    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    /************************************
     * 設定をクリックしてダイアログが表示される直前
     * @return 表示するダイアログのView
     */
    @Override
    protected View onCreateDialogView() {
Log.d("○○"+getClass().getSimpleName(),  "onCreateDialogView()");
        //// TimePicker（時刻）ダイアログを表示するので、そのViewを返す
        this.picker = new TimePicker(this.getContext());

        // 24時間表示にする、ここらへんカスタム属性で設定できるようになったらいいな
        this.picker.setIs24HourView(true);

        return (this.picker);
    }

    /************************************
     * @param v 生成するビュー、ここでは時刻ダイアログ
     */
    @Override
    protected void onBindDialogView(View v) {
Log.d("○○"+getClass().getSimpleName(), "onBindDialogView()");
        super.onBindDialogView(v);
        picker.setCurrentHour(
                calendar.get(Calendar.HOUR_OF_DAY) // 時間を取得（24時間）
        );
        // TODO これはAPIレベル23から非推奨になりました
        picker.setCurrentMinute(
                calendar.get(Calendar.MINUTE)   // 分を取得
        );
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            //// ダイアログの値をcalendarフィールドにセット
            this.calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            this.calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            //// サマリーをセット
            this.setSummary(this.getSummaryStr());

            //// 値をSPに保存
            // 値を変更したというリスナーを叩いて、保存すべきというときだけ保存する
            if ( this.callChangeListener(calendar.getTimeInMillis()) ) {
                this.persistLong(
                        this.calendar.getTimeInMillis()  //元期からの UTC ミリ秒値で表される現在の時間。
                );

                // SPに値を保存したらコレを呼ばないとダメ
                this.notifyChanged();
            }
        }
    }

    /**********************************
     * onSetInitialValue()にXMLからデフォルト値を提供する, コンストラクタでsuper() したときに呼ばれる
     * カスタム属性を使用する場合はここで値を渡すのがよい
     * ※XMLにデフォルト値が設定されていないときは呼ばれない
     * @param tArray <Preference>の属性の全ての配列
     * @param index  <Preference>の属性配列に対する「defaultValue」属性のインデックス
     * @return このクラスのデフォルト値 UTCのlong型
     */
    @Override
    protected Object onGetDefaultValue(TypedArray tArray, int index) {
Log.d("○○"+this.getClass().getSimpleName(), "onGetDefaultValue(): " + tArray.getString(index));


        String defaultValue = tArray.getString(index);
        try {
            return new SimpleDateFormat("HH:mm").parse(defaultValue).getTime();
        } catch (ParseException e) {
            return null;
        }
    }


    // TODO XMLのDefaultValueを取得できるようにする
    public static long getDefaultSpValue(){
        return System.currentTimeMillis();
    }

    /************************************
     * 現在の値を初期化する、コンストラクタの処理終了の後に呼ばれる、設定画面が表示された瞬間に呼ばれる
     * @param restorePersistedValue true: 保存された設定値があるとき、false: ないとき
     * @param defaultValue デフォルト値、this.onGetDefaultValue()の戻り値（保存された値がある場合は常にnullになる）
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
Log.d("○○"+this.getClass().getSimpleName(), "onSetInitialValue(): " + restorePersistedValue);

        //// 設定を表示した瞬間の値を生成 → calendarフィールドにセット
        if (restorePersistedValue) {    //SPに保存した値があるとき
            this.calendar.setTimeInMillis(
                    // SPに保存された値を取得、引数はデフォルト値（デフォルト値は呼ばれないが関数の使用上仕方なく記述）
                    this.getPersistedLong(getDefaultSpValue())
            );

        } else {    // SPに保存した値がないとき
            if (defaultValue == null) { // XMLにdefaultValue属性がないとき
               this.calendar.setTimeInMillis(
                        getDefaultSpValue()
                );
            } else {                    // XMLにdefaultValue属性があるとき

 Log.d("ffffff", (long)defaultValue + "" );
                this.calendar.setTimeInMillis(
                        // XMLに設定されているデフォルト値
                        (long)defaultValue
                );
            }
        }

        //// 設定を表示した瞬間の値をサマリーにセット
        this.setSummary(getSummaryStr());
    }

// TODO 回転時のあれをちゃんとする

    /************************************
     * フィールドからサマリー用の文字列を生成する
     * @return String サマリーに表示する時刻
     */
    @Nullable
    private CharSequence getSummaryStr() {
        if (calendar == null) {
            return null;
        }
        return DateFormat.getTimeFormat( this.getContext() )
                .format(
                    new Date(
                            //元期からの UTC（世界協定時間）ミリ秒値で表される現在の時間。
                            calendar.getTimeInMillis()
                    )
                );
    }

}

