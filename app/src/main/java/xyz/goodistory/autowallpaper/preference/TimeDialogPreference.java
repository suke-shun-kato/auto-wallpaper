package xyz.goodistory.autowallpaper.preference;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/************************************
 * (参考URL)↓
 * https://stackoverflow.com/questions/5533078/timepicker-in-preferencescreen/34398747
 *
 * ■preferences.xml の書き方
 * <xyz.goodistory.autowallpaper.preference.TimeDialogPreference
 *     android:key="aaaaaaaaaa"           // key名
 *     android:title="タイトルですよ"     // タイトルの文字列
 *     android:defaultValue="11:11"    // デフォルト値
 *     />
 * android:summary は設定しない。設定された値が表示されるので
 *
 * ■SPに保存される値
 *  元期からの UTC（協定世界時、世界標準時間）をミリ秒値で表される時間 long型
 *
 */
public class TimeDialogPreference extends DialogPreference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private long mUnixTime;


    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public TimeDialogPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TimeDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeDialogPreference(Context context) {
        super(context);
    }

    // --------------------------------------------------------------------
    // Getter
    // --------------------------------------------------------------------
    private long getUnixTime() {
        return mUnixTime;
    }

    // --------------------------------------------------------------------
    // Override
    // --------------------------------------------------------------------
    /************************************
     * 現在の値を初期化するときに呼ばれる
     * コンストラクタの処理終了の後に呼ばれる、設定画面が表示された瞬間に呼ばれる
     * @param defaultValue 保存された値がない場合: onGetDefaultValue()の戻り値
     *                      保存された値がある場合: null
     */
    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        long unixTimeMsec;
        if (defaultValue == null) { // SharedPreferenceに値が保存されている場合
            unixTimeMsec = getPersistedLong(0);
        } else {
            unixTimeMsec = (long)defaultValue;
        }
        setAndPersistUnitTime(unixTimeMsec);
    }

   /**********************************
    * preferences.xml から defaultValue を取得するときに呼ばれる
    * defaultValue がない場合は呼ばれない
    * コンストラクタでsuper() したときに呼ばれる
    * defaultValue の値の加工
    *
    * @param a <Preference>の属性の全ての配列
    * @param index  <Preference>の属性配列に対する「defaultValue」属性のインデックス
    * @return  mDefaultValue にセットされる値, onSetInitialValue() に提供される値
    */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String defaultValue = a.getString(index);

        try {
            // Dateクラス（年、月、日、時間、分、秒のクラス）を取得
            Date date = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(defaultValue);

            // unix time ミリ秒を取得
            return date.getTime();
        } catch (ParseException e) {
            return null;
        }
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    /**
     * フィールドにセットして SharedPreference に保存
     * @param unixTimeMsec 保存したい値
     */
    private void setAndPersistUnitTime(Long unixTimeMsec) {
        if (mUnixTime != unixTimeMsec) {
            // SharedPreferenceに保存
            persistLong(unixTimeMsec);

            mUnixTime = unixTimeMsec;

            // 変更したことを知らせて、リスナーを実行
            notifyChanged();
        }
    }

    /**
     * SharedPreferenceから文字列として値を取得
     * @return 時刻
     */
    public String getPersistedAsText() {
        long unixTime = getPersistedLong(0);
        return String.format( Locale.getDefault(),"%d:%02d",
                unixTimeToHour(unixTime), unixTimeToMinute(unixTime));
    }

    /**
     * SharedPreferenceの値からサマーリーをセット
     */
    public void setSummaryFromPersistedValue() {
        setSummary( getPersistedAsText() );
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    private static int unixTimeToHour(long unixTimeMsec) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(unixTimeMsec);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    private static int unixTimeToMinute(long unixTimeMsec) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(unixTimeMsec);
        return calendar.get(Calendar.MINUTE);
    }
    private static long toUnixTime(int hourOfDay, int minute) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(1970,1,1, hourOfDay, minute);
        return calendar.getTimeInMillis();
    }
    // --------------------------------------------------------------------
    // class
    // --------------------------------------------------------------------
    /**
     * Timerのダイアログ
     * コンストラクタは不要
     */
    public static class Dialog extends PreferenceDialogFragmentCompat {
        private static final String SAVE_STATE_HOUR = "TimeDialogPreference.Dialog.HourOfDay";
        private static final String SAVE_STATE_MINUTE = "TimeDialogPreference.Dialog.Minute";

        private int mHourOfDay;
        private int mMinute;

        private TimePicker mTimePicker;

        /**
         * preferenceのkey名を保存してインスタンスを取得,
         * 他のPreferenceDialogFragmentと同じ書き方
         * @param key このダイアログをshowするPreferenceのkey名
         * @return インスタンス
         */
        public static Dialog newInstance(String key) {
            // インスタンス作成
            final Dialog dialog = new Dialog();

            // Preferenceのkey名をfragment付きのBundleに保存, destroyされても保存される
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            dialog.setArguments(b);

            return dialog;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                long dialogUnixTime = getTimeDialogPreference().getUnixTime();
                mHourOfDay = unixTimeToHour(dialogUnixTime);
                mMinute = unixTimeToMinute(dialogUnixTime);
            } else {    // 画像回転時
                mHourOfDay = savedInstanceState.getInt(SAVE_STATE_HOUR);
                mMinute = savedInstanceState.getInt(SAVE_STATE_MINUTE);
            }
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);

            int minute;
            int hourOfDay;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                minute = mTimePicker.getMinute();
                hourOfDay = mTimePicker.getHour();
            } else {
                minute = mTimePicker.getCurrentMinute();
                hourOfDay = mTimePicker.getCurrentHour();
            }

            outState.putInt(SAVE_STATE_HOUR, hourOfDay);
            outState.putInt(SAVE_STATE_MINUTE, minute);
        }

        /**
         * Creates the content view for the dialog (if a custom content view is
         * required). By default, it inflates the dialog layout resource if it is
         * set.
         *
         * @param context コンテキスト
         * @return The content View for the dialog.
         * @see DialogPreference#setLayoutResource(int)
         */
        @Override
        protected View onCreateDialogView(Context context) {
            mTimePicker = new TimePicker(context);
            mTimePicker.setIs24HourView(true);

            // TimerPickerの設定を実行
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTimePicker.setHour(mHourOfDay);
                mTimePicker.setMinute(mMinute);
            } else {
                mTimePicker.setCurrentHour(mHourOfDay);
                mTimePicker.setCurrentMinute(mMinute);
            }

            return mTimePicker;
        }

        /**
         * 値を保存するのに使用
         * @param positiveResult OKボタンを押したか
         */
        @Override
        public void onDialogClosed(boolean positiveResult) {

            if (positiveResult) {
                //// unixTime を取得
                long unixTimeMsec;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    unixTimeMsec = toUnixTime(mTimePicker.getHour(), mTimePicker.getMinute());
                } else {
                    unixTimeMsec = toUnixTime(mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                }

                //// SPに保存
                TimeDialogPreference timeDialogPreference = getTimeDialogPreference();
                timeDialogPreference.setAndPersistUnitTime(unixTimeMsec);
            }
        }

        // ----------------------------------
        // 処理をまとめただけのもの
        // ----------------------------------
        private TimeDialogPreference getTimeDialogPreference() {
            return (TimeDialogPreference) getPreference();
        }


    }
}

