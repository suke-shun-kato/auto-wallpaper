///*
//■XMLの設定の仕方
//<xyz.goodistory.autowallpaper.TimePreference
//    android:key="aaaaaaaaaa"           // key名
//    android:title="タイトルですよ"     // タイトルの文字列
//    android:defaultValue="11:11:11"    // デフォルト値　
//    />
//android:summary は設定しない。設定された値が表示されるので
//
//■SPに保存される値
// 元期からの UTC（協定世界時、世界標準時間）をミリ秒値で表される時間 long型
//
//■SPに値が保存されていないときは↓の関数を呼ぶ
// TimePreference.getDefaultSpValue()
//
//■下記のエラーが出るが修正しようがなさそうなので放置、もしかして公式のエラー？
// E/TimePickerClockDelegate: Unable to find keycodes for AM and PM.
//
//*/
//
//package xyz.goodistory.autowallpaper;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.os.Parcel;
//import android.os.Parcelable;
//import androidx.preference.DialogPreference;
//import androidx.annotation.Nullable;
//import android.text.format.DateFormat;
//import android.util.AttributeSet;
//import android.view.View;
//import android.widget.TimePicker;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.GregorianCalendar;
//
///************************************
// * TODO preference packageに移動する
// * TODO androidxのpreferenceに対応させる
// * (参考URL)↓
// * https://stackoverflow.com/questions/5533078/timepicker-in-preferencescreen/34398747
// *
// * 公式のカスタムPreferenceの作成の方法マニュアル
// * https://developer.android.com/guide/topics/ui/settings?hl=ja#Custom
// *
// */
//public class TimePreference extends DialogPreference {
//    // --------------------------------------------------------------------
//    // フィールド
//    // --------------------------------------------------------------------
//    // 時間取得するピッカーのビューと内部的な値を保存するフィールド
//    private MyTimePicker picker;
//
//    // --------------------------------------------------------------------
//    // コンストラクタ
//    // --------------------------------------------------------------------
//    public TimePreference(Context context, AttributeSet attrs) {
//        super(context, attrs);
//
//
//        //// 時刻ダイアログの設定をする
//        // ↓は不必要自分が作成したダイアログのレイアウトXMLを使うわけじゃないので不必要、
//        //     代わりにonCreateDialogView()で既存のダイアログを生成している
//        // setDialogLayoutResource(R.layout.xxxx );
//
//        this.setPositiveButtonText(android.R.string.ok);
//        this.setNegativeButtonText(android.R.string.cancel);
//        this.setDialogIcon(null);
//
//        ////
//        this.picker = new TimePreference.MyTimePicker(this.getContext());
//
//    }
//
//    // --------------------------------------------------------------------
//    // カスタムTimePickerを定義
//    // --------------------------------------------------------------------
//    private static class MyTimePicker extends TimePicker {
//        public MyTimePicker(Context context) {
//            super(context);
//        }
//
//
//        /************************************
//         * ミリ秒をTimePickerにセットする
//         */
//        @SuppressWarnings("WeakerAccess")
//        public void setMilliTime(long msec) {
//            Calendar calendar = new GregorianCalendar();
//            calendar.setTimeInMillis(msec);
//
//            this.setCurrentHour( calendar.get(Calendar.HOUR_OF_DAY) );
//            this.setCurrentMinute( calendar.get(Calendar.MINUTE) );
//        }
//
//        /************************************
//         * TimePickerの時間をミリ秒へ変換
//         * @return 変換した時間
//         */
//        @SuppressWarnings("WeakerAccess")
//        public long getMilliTime() {
//            Calendar calendar = new GregorianCalendar();
//
//            calendar.set(Calendar.HOUR_OF_DAY, this.getCurrentHour());
//            calendar.set(Calendar.MINUTE, this.getCurrentMinute());
//
//            return calendar.getTimeInMillis();
//
//        }
//    }
//
//    // --------------------------------------------------------------------
//    // static
//    // --------------------------------------------------------------------
//    /************************************
//     * XMLにデフォルト値がない場合のデフォルト値
//     * sharedPref.getString() の引数のデフォルト値としても使用できる
//     */
//    @SuppressWarnings("WeakerAccess")
//    public static long getDefaultSpValue(){
//        return System.currentTimeMillis();
//    }
//
//    // --------------------------------------------------------------------
//    //
//    // --------------------------------------------------------------------
//    /************************************
//     * 設定をクリックしてダイアログが表示される直前
//     * @return 表示するダイアログのView
//     */
//    @Override
//    protected View onCreateDialogView() {
//        //// pickerインスタンスを新しく生成→設定→フィールドにセット
//        MyTimePicker newPicker = new MyTimePicker(this.getContext());
//
//        newPicker.setIs24HourView(true);
//
//        // TimePicker に時間をセット
//        newPicker.setMilliTime(
//                // SP からデータを取得、ない場合はデフォルト値
//                this.getPersistedLong(getDefaultSpValue())
//        );
//        ////
//        this.picker = newPicker;
//
//        return (this.picker);
//    }
//
//
//    /************************************
//     * ダイアログが閉じるとき、ここでSPに保存する
//     * @param positiveResult 押されたボタンは・・・
//     */
//    @Override
//    protected void onDialogClosed(boolean positiveResult) {
//        super.onDialogClosed(positiveResult);
//
//        if (positiveResult) {
//            //// サマリーをセット
//            this.setSummary(this.getSummaryStr());
//
//            //// 値をSPに保存
//            // 値を変更したというリスナーを叩いて、保存すべきというときだけ保存する
//            long msec = this.picker.getMilliTime();//元期からの UTC ミリ秒値で表される現在の時間。
//            if ( this.callChangeListener(msec) ) {
//                this.persistLong( msec );
//                // SPに値を保存したらコレを呼ばないとダメ
//                this.notifyChanged();
//            }
//        }
//    }
//
//    /**********************************
//     * onSetInitialValue()にXMLからデフォルト値を提供する, コンストラクタでsuper() したときに呼ばれる
//     * カスタム属性を使用する場合はここで値を渡すのがよい
//     * ※XMLにデフォルト値が設定されていないときは呼ばれない
//     * @param tArray <Preference>の属性の全ての配列
//     * @param index  <Preference>の属性配列に対する「defaultValue」属性のインデックス
//     * @return このクラスのデフォルト値 UTCのlong型
//     */
//    @SuppressLint("SimpleDateFormat")
//    @Override
//    protected Object onGetDefaultValue(TypedArray tArray, int index) {
//
//        String defaultValue = tArray.getString(index);
//        try {
//            return new SimpleDateFormat("HH:mm").parse(defaultValue).getTime();
//        } catch (ParseException e) {
//            return null;
//        }
//    }
//
//
//    /************************************
//     * 現在の値を初期化する、コンストラクタの処理終了の後に呼ばれる、設定画面が表示された瞬間に呼ばれる
//     * @param restorePersistedValue true: 保存された設定値があるとき、false: ないとき
//     * @param defaultValue デフォルト値、this.onGetDefaultValue()の戻り値（保存された値がある場合は常にnullになる）
//     */
//    @Override
//    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
//        //// 設定を表示した瞬間の値を生成 → calendarフィールドにセット
//        if (restorePersistedValue) {    //SPに保存した値があるとき
//            this.picker.setMilliTime(
//                    // SPに保存された値を取得、引数はデフォルト値（デフォルト値は呼ばれないが関数の使用上仕方なく記述）
//                    this.getPersistedLong(getDefaultSpValue())
//            );
//
//        } else {    // SPに保存した値がないとき
//            if (defaultValue == null) { // XMLにdefaultValue属性がないとき
//                this.picker.setMilliTime(
//                        getDefaultSpValue()
//                );
//
//
//            } else {                    // XMLにdefaultValue属性があるとき
//                this.picker.setMilliTime(
//                        // XMLに設定されているデフォルト値
//                        (long)defaultValue
//                );
//            }
//            //// SPにTimerPickerの値を保存する（ここの保存は、Android標準のPreferenceと処理を揃えるために必ずすること）
//            this.persistLong(
//                    this.picker.getMilliTime()
//            );
//        }
//
//        //// 設定を表示した瞬間の値をサマリーにセット
//        this.setSummary( this.getSummaryStr() );
//    }
//
//
//    /************************************
//     * フィールドからサマリー用の文字列を生成する
//     * @return String サマリーに表示する時刻
//     */
//    @Nullable
//    private CharSequence getSummaryStr() {
//        if (this.picker == null) {
//            return null;
//        }
//        return DateFormat.getTimeFormat( this.getContext() )
//                .format(
//                    new Date(
//                            //元期からの UTC（世界協定時間）ミリ秒値で表される現在の時間。
//                            this.picker.getMilliTime()
//                    )
//                );
//    }
//
//    // --------------------------------------------------------------------
//    // onSaveInstanceState()やonRestoreInstanceState()で使う為のサブクラス
//    // --------------------------------------------------------------------
//    private static class SavedState extends BaseSavedState {
//        // Member that holds the setting's value
//        // Change this data type to match the type saved by your Preference
//        long value;
//
//        @SuppressWarnings("WeakerAccess")
//        public SavedState(Parcelable superState) {
//            super(superState);
//        }
//
//        @SuppressWarnings("WeakerAccess")
//        public SavedState(Parcel source) {
//            super(source);
//            // Get the current preference's value
//            this.value = source.readLong();  // Change this to read the appropriate data type
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            super.writeToParcel(dest, flags);
//            // Write the preference's value
//            dest.writeLong(this.value);  // Change this to write the appropriate data type
//        }
//
//        // Standard creator object using an instance of this class
//        public static final Parcelable.Creator<SavedState> CREATOR =
//                new Parcelable.Creator<SavedState>() {
//
//                    public SavedState createFromParcel(Parcel in) {
//                        return new SavedState(in);
//                    }
//
//                    public SavedState[] newArray(int size) {
//                        return new SavedState[size];
//                    }
//                };
//    }
//
//    // --------------------------------------------------------------------
//    // onSaveInstanceState()やonRestoreInstanceState()
//    // --------------------------------------------------------------------
//    @Override
//    protected Parcelable onSaveInstanceState() {
//        // ----------------------------------
//        // スーパークラスのParcelable
//        // ----------------------------------
//        final Parcelable superState = super.onSaveInstanceState();
//
//
//        // ダイアログが表示されていないときは親クラスのメソッドを実行
//        if (this.getDialog() == null) {
//            // インスタンスの状態を回転時に保存する必要がないので、
//            // DialogPreferenceの戻り値のParcelableを返す
//            return superState;
//        }
//
//        // ----------------------------------
//        // 内部クラスのParcelable
//        // ----------------------------------
//        // Create instance of custom BaseSavedState
//        final SavedState myState = new SavedState(superState);
//        // Set the state's value with the class member that holds current
//        // setting value
//
//        myState.value = this.picker.getMilliTime(); //long型に変換して保存
//        return myState;
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        // Check whether we saved the state in onSaveInstanceState
//        if (state == null || !state.getClass().equals(SavedState.class)) {
//            // Didn't save the state, so call superclass
//            super.onRestoreInstanceState(state);
//            return;
//        }
//
//        // Cast state to custom BaseSavedState and pass to superclass
//        SavedState myState = (SavedState) state;
//        super.onRestoreInstanceState(myState.getSuperState());
//
//        // Set this Preference's widget to reflect the restored state
//        this.picker.setMilliTime(myState.value);
//
//    }
//
//}
//
