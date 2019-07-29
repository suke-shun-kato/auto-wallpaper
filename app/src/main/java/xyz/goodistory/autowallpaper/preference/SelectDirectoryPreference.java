package xyz.goodistory.autowallpaper.preference;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import xyz.goodistory.autowallpaper.R;
import xyz.goodistory.autowallpaper.util.FileExtended;

/**
 * ディレクトリを選択するPreference
 * Created by k-shunsuke on 2017/12/08.
 */
public class SelectDirectoryPreference extends DialogPreference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 設定値のディレクトリパス */
    private String mDirectoryPath = null;

    /** デフォルトのディレクトリパス,ここはnullで初期化しないこと */
    private String mDefaultDirectoryPath;

    /** ダイアログのビューのルート */
    private View mDialogDirView = null;

    /** パーミッション許可ダイアログを表示するかどうか */
    private final boolean mShowsPermissionDialog;

    /** onRequestPermissionsResult() でどのパーミッション許可Dialogから呼んだか判別するためのもの */
    private final int mPermissionDialogRequestCode;
    private final String mPermissionRationaleDialogText;



    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** ダイアログのレイアウトファイルのID */
    private static final int R_LAYOUT_DIR_PREF = R.layout.dialog_select_dir_pref;

    /** ダイアログのレイアウトXML内の現在のディレクトリのパスを表示する要素のID */
    private static final int R_ID_DIALOG_CURRENT_PATH = R.id.dirDialog_current_path;

    /** ダイアログのレイアウトXML内の現在のディレクトリ一覧を表示する要素のID */
    private static final int R_ID_DIALOG_FILE_LIST = R.id.dirDialog_file_list;

    /** XMLでデフォルト値が設定されていないときのデフォルト値 */
    public static final String DEFAULT_DIR_PATH_WHEN_NO_DEFAULT
            = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
            + System.getProperty("file.separator");

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    /************************************
     * コンストラクタ
     * @param context このPreferenceのコンテキスト
     * @param attrs XMLの属性のセット
     */
    public SelectDirectoryPreference(Context context, AttributeSet attrs) {
        // XMLにデフォルト値があるなら、onGetDefaultValue() がここで呼ばれる
        super(context, attrs);

        // ----------------------------------
        // XMLでデフォルト値が設定されていない場合のデフォルト値の設定
        // --------------------------------
        // このクラスのデフォルト値の設定
        if (mDefaultDirectoryPath == null) {
            mDefaultDirectoryPath = DEFAULT_DIR_PATH_WHEN_NO_DEFAULT;
            setDefaultValue(DEFAULT_DIR_PATH_WHEN_NO_DEFAULT);
        }
        // ----------------------------------
        // ダイアログの設定
        // ----------------------------------
        // ダイアログのタイトルを設定
        final TypedArray typedAry = context.obtainStyledAttributes(
                attrs, R.styleable.SelectDirectoryPreference);
        try {
            // ダイアログのタイトル
            final String dialogTitle
                    = typedAry.getString(R.styleable.SelectDirectoryPreference_dialogTitle);
            setDialogTitle(dialogTitle);

            // パーミッション許可ダイアログを表示するか
            mShowsPermissionDialog = typedAry.getBoolean(
                    R.styleable.SelectDirectoryPreference_showsPermissionDialog, false);

            // パーミッション許可ダイアログのリクエストコード
            mPermissionDialogRequestCode = typedAry.getInt(
                    R.styleable.SelectDirectoryPreference_permissionDialogRequestCode, 0);

            if (mShowsPermissionDialog && mPermissionDialogRequestCode == 0) {
                throw new NullPointerException("No permissionDialogRequestCode attribute");
            }

            // パーミッション許可必要説明ダイアログの本文
            mPermissionRationaleDialogText = typedAry.getString(
                    R.styleable.SelectDirectoryPreference_permissionRationaleDialogText);
        } finally {
            typedAry.recycle();
        }

        // OKボタンとCancelボタンの「文字列（テキスト）」を設置
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        // ダイアログのアイコンを設定、アイコンなし
        setDialogIcon(null);
    }

    // --------------------------------------------------------------------
    // メソッド、ダイアログ関係
    // --------------------------------------------------------------------
    @Override
    protected void onClick() {
        final AppCompatActivity activity = (AppCompatActivity)getContext();
        if (activity == null) {
            throw new IllegalStateException("activity is null");
        }
        final String permissionReadExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        // android 5.1 以前だと常にtrueになる、アプリインストール時にパーミッション許可を得るので
        final int permissionStatus = ContextCompat.checkSelfPermission(
                getContext(), permissionReadExternalStorage);
        if ( !mShowsPermissionDialog || permissionStatus == PackageManager.PERMISSION_GRANTED) {
        // パーミッション許可ダイアログ表示しない場合、
        // もしくは表示するときでパーミッション許可されている場合
            super.onClick();    //ここでonCreateDialogView()が呼ばれる

        } else {
        // 許可されていない場合
            // Rationale: 根拠
            boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, permissionReadExternalStorage);
            if (shouldShowRationale) {
            // 説明理由の表示が必要な場合
                // 説明ダイアログに渡すための値をセット
                Bundle bundle = new Bundle();
                bundle.putString(
                        RationaleDialogFragment.BUNDLE_KEY_TEXT, mPermissionRationaleDialogText);
                bundle.putInt(
                        RationaleDialogFragment.BUNDLE_KEY_PERMISSION_DIALOG_REQUEST_CODE,
                        mPermissionDialogRequestCode);

                // パーミッション必要理由の説明ダイアログを表示
                RationaleDialogFragment dialog = new RationaleDialogFragment();
                dialog.setArguments(bundle);
                dialog.show(activity.getSupportFragmentManager(),
                        RationaleDialogFragment.FRAGMENT_TAG_NAME);

            } else {
            // 説明理由の表示が必要でない場合、初回など

                // パーミッション許可ダイアログを表示
                final String[] permissions = new String[] {
                        permissionReadExternalStorage,
                };
                ActivityCompat.requestPermissions(
                        activity, permissions, mPermissionDialogRequestCode);
            }
        }
    }


    /**
     * パーミッションの許可が必要な理由を説明するダイアログ
     */
    public static class RationaleDialogFragment extends android.support.v4.app.DialogFragment {
        /** 表示するテキスト */
        static final String FRAGMENT_TAG_NAME = RationaleDialogFragment.class.getSimpleName();

        /** 本文をBundleから取得するためのkey */
        static final String BUNDLE_KEY_TEXT = "text";
        static final String BUNDLE_KEY_PERMISSION_DIALOG_REQUEST_CODE = "permission_code";

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //// activity を取得
            Activity activity = getActivity();
            if (activity == null) {
                throw new IllegalStateException("activity is null");
            }

            //// bundle
            Bundle bundle = getArguments();

            // 本文を取得
            if ( !bundle.containsKey(BUNDLE_KEY_TEXT) ) {
                throw new IllegalStateException("Not contains key BUNDLE_KEY_TEXT. Set bundle key.");
            }
            final String text = bundle.getString(BUNDLE_KEY_TEXT);

            // リクエストコード
            if ( !bundle.containsKey(BUNDLE_KEY_PERMISSION_DIALOG_REQUEST_CODE) ) {
                throw new IllegalStateException(
                        "Not contains key BUNDLE_KEY_PERMISSION_DIALOG_REQUEST_CODE. Set bundle key.");
            }
            final int permissionDialogRequestCode = bundle.getInt(BUNDLE_KEY_PERMISSION_DIALOG_REQUEST_CODE);

            //// ダイアログ作成
            return new AlertDialog.Builder(activity)
                    .setMessage(text)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Activity activity = getActivity();
                            if (activity == null) {
                                throw new IllegalStateException("Activity is null");
                            }

                            String[] permissions = new String[] {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            };

                            ActivityCompat.requestPermissions(
                                    activity, permissions, permissionDialogRequestCode);
                        }
                    })
                    .create();
        }


    }

    /**
     * Activityでの onRequestPermissionsResult() の中で実行するメソッド
     * パーミッション許可ダイアログが閉じられたときに実行するコールバック
     *
     * @param requestCode どの許可ダイアログからのコールバックか判別するためのコード
     * @param permissions どのパーミッションを許可したかの配列
     * @param grantResults パーミッション許可ダイアログ
     */
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == mPermissionDialogRequestCode
                && permissions.length == 1
                && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {  // パーミッション許可ダイアログでOKを押されたとき
            onClick();
        }
    }

    /**
     * ダイアログのViewが生成されるとき
     * @return このViewがダイアログに表示される
     */
    @Override
    protected View onCreateDialogView()  {
        // ----------------------------------
        // 初期化
        // ----------------------------------
        mDirectoryPath = getPersistedString(mDefaultDirectoryPath);
        mDialogDirView = LayoutInflater.from(getContext())
                .inflate(R_LAYOUT_DIR_PREF, null);

        // ----------------------------------
        // ダイアログの表示を更新（初期化）する
        // ----------------------------------
        updateDialogDisplay(mDirectoryPath, this);

        // ----------------------------------
        // ダイアログ表示に対するイベントリスナーの設置
        // ----------------------------------
        ListView dirListLv = mDialogDirView.findViewById(R_ID_DIALOG_FILE_LIST);
        dirListLv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    /************************************
                     * @param parentAdapterView クリックされたListItemのビュー
                     * @param view クリックされた<item>のView
                     * @param i クリックされたビューのアダプターでの位置（index）
                     * @param l クリックされたビューの行の順番、だいたいはiと同じ
                     */
                    @Override
                    public void onItemClick(AdapterView<?> parentAdapterView, View view, int i, long l) {
                        // ----------------------------------
                        // ファイルをクリックしたときの処理、途中で処理を切り上げ
                        // ----------------------------------
                        if ( !((TextView)view).getText().toString()
                                .endsWith( System.getProperty("file.separator") )   ) {
                            return;
                        }

                        // ----------------------------------
                        // ディレクトリをクリックしたときの処理
                        // ----------------------------------
                        //// ダイアログの表示を更新する
                        updateDialogDisplay(mDirectoryPath + ((TextView)view).getText(),
                                SelectDirectoryPreference.this
                        );

                    }
                }
        );

        // ----------------------------------
        // このViewが表示にセットされる
        // ----------------------------------
        return mDialogDirView;
    }

    /************************************
     * 指定のディレクトリパスでのファイル一覧にダイアログを更新する
     * @param dirPath このディレクトリに画面を更新, 正規化されていなくても正規化されるのでOK
     * @param context このオブジェクトを更新する
     */
    private void updateDialogDisplay(String dirPath, SelectDirectoryPreference context) {
        File newDirFile = new File(dirPath);

        // ----------------------------------
        // 例外処理
        // ----------------------------------
        if ( !newDirFile.exists() ) {   //ディレクトリがないとき作成する
            boolean canMakeDir = newDirFile.mkdirs();
            if ( !canMakeDir ) {
                throw new IllegalStateException ("dirPathのディレクトリを作成できませんでした。");
            }
        }
        if ( !newDirFile.isDirectory() ) {
            throw new IllegalStateException ("dirPathがディレクトリではありません。");
        }

        if ( newDirFile.list() == null ) {
            throw new IllegalStateException ("dirPathのファイル一覧を取得できません。");

        }

        // ----------------------------------
        // 初期化
        // ----------------------------------
        //// ディレクトリパスを正規化する
        String normalizedDirPath;  // 正規化されたディレクトリパス


        try {
            // 正規パス名を取得
            // 現在のディレクトリが"/"の場合は"/"、"dir1/.."などでルートのときは""空文字列が返る）
            String normalizedPath = newDirFile.getCanonicalPath();
            if (normalizedPath.length() != 0 && normalizedPath.charAt(normalizedPath.length()-1) == '/') {
            // 末尾が"/"の場合はそのまま
                normalizedDirPath = normalizedPath;
            } else {
            // 末尾に/がないとき、空文字列のときは/を追加、ルートディレクトリ以外はここを通る
                normalizedDirPath = normalizedPath + System.getProperty("file.separator");
            }
        } catch (IOException e) {
            normalizedDirPath = this.mDefaultDirectoryPath;
        }

        //// 初期化
        context.mDirectoryPath = normalizedDirPath;
        TextView dirPathTextView = context.mDialogDirView.findViewById(R_ID_DIALOG_CURRENT_PATH);
        ListView dirListLv = context.mDialogDirView.findViewById(R_ID_DIALOG_FILE_LIST);

        // ----------------------------------
        // メイン処理
        // ----------------------------------
        //// 現在のディレクトリのパスをViewにセット
        dirPathTextView.setText(context.mDirectoryPath);

        //// ディレクトリ一覧をViewにセット

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context.getContext(),
                android.R.layout.simple_list_item_1,
                new FileExtended(context.mDirectoryPath).listDirFile(true)   //Sting[]、ファイル一覧
        );
        dirListLv.setAdapter(adapter);
    }

    /**
     * 設定の値を保存する、ダイアログが閉じたとき
     * @param positiveResult true:ユーザーがポジティブボタン（OKボタン）を押したとき,
     *                      false:ネガティブボタン、またはキャンセルボタンを押したとき
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // OKボタンを押してダイアログを閉じたとき選択ディレクトリパスを保存する
        if (positiveResult) {
            // 設定値を保存
            persistString(mDirectoryPath);
        }
    }


    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * 現在の値を初期化する、コンストラクタの処理終了の後に呼ばれる
     * ※このクラスのデフォルト値が設定されていない場合は呼ばれない
     * （onGetDefaultValue() で値がreturnされないとき、setDefaultValue() でデフォルト値が設定されていないとき
     * @param restorePersistedValue true: 保存された設定値があるとき、false: ないとき
     * @param defaultValue デフォルト値、this.onGetDefaultValue()の戻り値（保存された値がある場合は常にnullになる）
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if ( restorePersistedValue ) {
            mDirectoryPath = getPersistedString(null);
        } else {
            persistString( (String)defaultValue );
            mDirectoryPath = (String) defaultValue;
        }
    }

    /**********************************
     * onSetInitialValue()にデフォルト値を提供する, コンストラクタでsuper() したときに呼ばれる
     * ※XMLにデフォルト値が設定されていないときは呼ばれない
     * @param tArray <Preference>の属性の全ての配列
     * @param index  <Preference>の属性配列に対する「defaultValue」属性のインデックス
     * @return このクラスのデフォルト値
     */
    @Override
    protected Object onGetDefaultValue(TypedArray tArray, int index) {

        //// 特殊文字の時初期値を加工
        String defaultStr = tArray.getString(index);
        //noinspection ConstantConditions
        switch (defaultStr) {
            case "DCIM":
                this.mDefaultDirectoryPath = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .getAbsolutePath()
                        + System.getProperty("file.separator");
                break;
            case "PICTURES":
                this.mDefaultDirectoryPath = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath()
                        + System.getProperty("file.separator");
                break;
            default:
                this.mDefaultDirectoryPath = defaultStr;
                break;
        }
        return this.mDefaultDirectoryPath;
    }

    // --------------------------------------------------------------------
    // 回転時関連の処理
    // --------------------------------------------------------------------
    /******************************
     * 回転直前に値を保存する
     * @return 保存する値
     */
    @Override
    protected Parcelable onSaveInstanceState() {

        // ----------------------------------
        // スーパークラスのParcelable
        // ----------------------------------
        // 親クラス（DialogPreference）のメソッドの戻り値、Parcelable
        final Parcelable superStatParcelable = super.onSaveInstanceState();

        // ダイアログが表示されていないときは親クラスのメソッドを実行
        if (this.getDialog() == null) {
            // インスタンスの状態を回転時に保存する必要がないので、
            // DialogPreferenceの戻り値のParcelableを返す
            return superStatParcelable;
        }

        // ----------------------------------
        // 内部クラスのParcelable
        // ----------------------------------
        // 内部クラスのParcelableを作成
        final MySavedState myState = new MySavedState(superStatParcelable);

        // Parcelableの設定を行う
        myState.value = this.mDirectoryPath;

        // 返す
        return myState;
    }

    /***********************************
     * 回転直後に値を読み出す
     * @param state par
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        // ----------------------------------
        // 値がないとき、スーパークラスのParcelableのとき（ダイアログが表示されていないとき）
        // ----------------------------------
        // onSaveInstanceState() で値をparcelableに保存したか確認
        if (state == null || !state.getClass().equals(MySavedState.class)) {
            // 保存していないときは親クラスを呼ぶだけ
            super.onRestoreInstanceState(state);
            return;
        }

        // ----------------------------------
        // 内部クラスのParcelable（ダイアログが表示されているとき）
        // ----------------------------------
        // Cast state to custom BaseSavedState and pass to superclass
        MySavedState myState = (MySavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // この時点でダイアログは表示されているので、あとは値を設定して表示するだけ
        this.mDirectoryPath = myState.value;
        this.updateDialogDisplay(this.mDirectoryPath,this);    // 表示を更新
    }

    // ------------------------------
    // 内部クラス
    // ------------------------------
    /**
     * Parcelableの役割のクラス（ParcelableはParcelに保存するためのクラス、ParcelはHDDのようなものと考えればいい、
     * Parcelは小包という意味）
     * このクラスもPreference.BaseSavedStateクラスもParcelableクラスをimplements
     *
     * https://developer.android.com/guide/topics/ui/settings.html?hl=ja
     */
    @SuppressWarnings("WeakerAccess")
    private static class MySavedState extends Preference.BaseSavedState {
        // --------------------------------------------------------------------
        // フィールド
        // --------------------------------------------------------------------
        /** Parcelに渡して保存する値 */
        String value;

        /************************************
         * Parcelable.Creatorクラスを継承したクラス（サブクラス）のインスタンス
         */
        public static final Parcelable.Creator<MySavedState> CREATOR =
                new Parcelable.Creator<MySavedState>() {

                    /************************************
                     * 内部クラスであるSavedStateのインスタンスを作成するメソッド
                     * @param in 保存先のParcel（小包）
                     */
                    public MySavedState createFromParcel(Parcel in) {
                        return new MySavedState(in);
                    }

                    /************************************
                     * 内部クラスであるSavedStateのインスタンスを複数作成するためのメソッド
                     * @param size 保存するParcelの数、おそらくこの数だけ this.createFromParcel()でSavedStateインスタンスが作成される
                     */
                    public MySavedState[] newArray(int size) {
                        return new MySavedState[size];
                    }
                };

        // --------------------------------------------------------------------
        // コンストラクタ
        // --------------------------------------------------------------------
        /************************************
         * コンストラクタ, スーパークラスの引数のとき
         * @param superState ラップするParcelable
         */
        @SuppressWarnings("WeakerAccess")
        public MySavedState(Parcelable superState) {
            super(superState);
        }


        /************************************
         * コンストラクタ, Parcelから
         * @param sourceParcel 値の取得対象のParcel
         */
        public MySavedState(Parcel sourceParcel) {
            super(sourceParcel);
            // Parcelから自身のParcelableに値を設定
            this.value = sourceParcel.readString();
        }

        // --------------------------------------------------------------------
        // メソッド
        // --------------------------------------------------------------------
        /************************************
         * Parcelに値を書き込むメソッド
         * @param destParcel 書き込む対象のParcel
         * @param flags ?、スーパークラスの同メソッドに渡すだけ
         */
        @Override
        public void writeToParcel(Parcel destParcel, int flags) {
            super.writeToParcel(destParcel, flags);
            // Write the preference's value
            destParcel.writeString(this.value);  // Change this to write the appropriate data type
        }

    }


}
