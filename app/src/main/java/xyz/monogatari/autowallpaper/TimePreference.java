package xyz.monogatari.autowallpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

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
            //// ダイアログの値をcalendarクラスに入れる
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());


            this.setSummary(this.getSummary());
            if (callChangeListener(calendar.getTimeInMillis())) {
                this.persistLong(
                        calendar.getTimeInMillis()  //元期からの UTC ミリ秒値で表される現在の時間。
                );
                notifyChanged();
            }
        }
    }

    /**********************************
     * onSetInitialValue()にデフォルト値を提供する, コンストラクタでsuper() したときに呼ばれる
     * カスタム属性を使用する場合はここで値を渡すのがよい
     * ※XMLにデフォルト値が設定されていないときは呼ばれない
     * @param tArray <Preference>の属性の全ての配列
     * @param index  <Preference>の属性配列に対する「defaultValue」属性のインデックス
     * @return このクラスのデフォルト値
     */
    @Override
    protected Object onGetDefaultValue(TypedArray tArray, int index) {
        return tArray.getString(index);
    }


    /************************************
     * 現在の値を初期化する、コンストラクタの処理終了の後に呼ばれる
     * ※このクラスのデフォルト値が設定されていない場合は呼ばれない
     * （onGetDefaultValue() で値がreturnされないとき、setDefaultValue() でデフォルト値が設定されていないとき
     * @param restorePersistedValue true: 保存された設定値があるとき、false: ないとき
     * @param defaultValue デフォルト値、this.onGetDefaultValue()の戻り値（保存された値がある場合は常にnullになる）
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        if (restorePersistedValue) {    //SPに保存した値があるとき
            if (defaultValue == null) {
                calendar.setTimeInMillis(
                        getPersistedLong(System.currentTimeMillis())
                );
            } else {
                calendar.setTimeInMillis(
                        Long.parseLong( getPersistedString((String) defaultValue) )
                );
            }
        } else {    // SPに保存した値がないとき
            if (defaultValue == null) {
                calendar.setTimeInMillis(
                        System.currentTimeMillis()
                );
            } else {
                calendar.setTimeInMillis(
                        Long.parseLong((String) defaultValue)
                );
            }
        }
        setSummary(getSummary());
    }
// TODO 回転時のあれをちゃんとする
    /************************************
     *
     *
     * @return String サマリーに表示する時刻
     */
    @Override
    public CharSequence getSummary() {
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

