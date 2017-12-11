package xyz.monogatari.suke.autowallpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import xyz.monogatari.suke.autowallpaper.util.FileExtended;

/**
 * ディレクトリを選択するPreference
 * Created by k-shunsuke on 2017/12/08.
 */
public class SelectDirPreference extends DialogPreference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 設定値のディレクトリパス */
    private String dirPath = null;

    /** デフォルトのディレクトリパス,ここはnullで初期化しないこと */
    private String dDirPath;

    /** ダイアログのビューのルート */
    private View dialogDirView = null;


    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** ダイアログのタイトルのリソースID */
    private static final int R_DIALOG_TITLE = R.string.select_dir_pref_dialog_title;
    /** ダイアログのレイアウトファイルのID */
    private static final int R_LAYOUT_DIR_PREF = R.layout.dialog_select_dir_pref;

    /** ダイアログのレイアウトXML内の現在のディレクトリのパスを表示する要素のID */
    private static final int R_ID_DIALOG_CURRENT_PATH = R.id.dirDialog_current_path;
    /** ダイアログのレイアウトXML内の現在のディレクトリ一覧を表示する要素のID */
    private static final int R_ID_DIALOG_FILE_LIST = R.id.dirDialog_file_list;

    /** XMLでデフォルト値が設定されていないときのデフォルト値 */
    private static final String DEFAULT_DIR_PATH_WHEN_NO_DEFAULT
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
    public SelectDirPreference(Context context, AttributeSet attrs) {
        super(context, attrs);  //XMLにデフォルト値があるなら、onGetDefaultValue() がここで呼ばれる
Log.d("○"+this.getClass().getSimpleName(), "コンストラクタ呼ばれた"+this.hashCode());

        // ----------------------------------
        // XMLでデフォルト値が設定されていない場合のデフォルト値の設定
        // --------------------------------
        // このクラスのデフォルト値の設定
        if (this.dDirPath == null) {
            this.dDirPath = DEFAULT_DIR_PATH_WHEN_NO_DEFAULT;
            this.setDefaultValue(DEFAULT_DIR_PATH_WHEN_NO_DEFAULT);
        }

        // ----------------------------------
        // ダイアログの設定
        // ----------------------------------
        // ダイアログのタイトルを設定
        this.setDialogTitle( this.getContext().getString(R_DIALOG_TITLE) );

        // OKボタンとCancelボタンの「文字列（テキスト）」を設置
        this.setPositiveButtonText(android.R.string.ok);
        this.setNegativeButtonText(android.R.string.cancel);

        // ダイアログのアイコンを設定、アイコンなし
        this.setDialogIcon(null);
    }

    // --------------------------------------------------------------------
    // メソッド、ダイアログ関係
    // --------------------------------------------------------------------
    /************************************
     * ダイアログのViewが生成されるとき
     * @return このViewがダイアログに表示される
     */
    @Override
    protected View onCreateDialogView()  {
        // ----------------------------------
        // 初期化
        // ----------------------------------
        this.dialogDirView = LayoutInflater.from(this.getContext())
                .inflate(R_LAYOUT_DIR_PREF, null);
        // ----------------------------------
        // ダイアログの表示を更新（初期化）する
        // ----------------------------------
        this.updateDialogDisplay(
                this.dirPath,
                this
        );

        // ----------------------------------
        // ダイアログ表示に対するイベントリスナーの設置
        // ----------------------------------
        ListView dirListLv = this.dialogDirView.findViewById(R_ID_DIALOG_FILE_LIST);
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
Log.d("○"+this.getClass().getSimpleName(), "i: "+ i);
Log.d("○"+this.getClass().getSimpleName(), "l: "+ l);
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
                        SelectDirPreference.this.updateDialogDisplay(
                                SelectDirPreference.this.dirPath + ((TextView)view).getText(),
                                SelectDirPreference.this
                        );

                    }
                }
        );

        // ----------------------------------
        // このViewが表示にセットされる
        // ----------------------------------
        return this.dialogDirView;
    }

    /************************************
     * 指定のディレクトリパスでのファイル一覧にダイアログを更新する
     * @param dirPath このディレクトリに画面を更新, 正規化されていなくても正規化されるのでOK
     * @param context このオブジェクトを更新する
     */
    private void updateDialogDisplay(String dirPath, SelectDirPreference context) {

        File newDirFile = new File(dirPath);

        // ----------------------------------
        // 例外処理
        // ----------------------------------
        if ( !newDirFile.isDirectory() || newDirFile.list() == null) {
            // newDirFile.list() == null はマニュフェストでストレージにアクセス権限を与えていなかったときに発生
            throw new IllegalStateException ("dirPathがディレクトリではありません。もしくはファイル一覧を取得できる権限がありません");
        }

        // ----------------------------------
        // 初期化
        // ----------------------------------
        //// ディレクトリパスを正規化する
        String normalizeDirPath;  // 正規化されたディレクトリパス
        try {
            normalizeDirPath = newDirFile.getCanonicalPath() + System.getProperty("file.separator");
        } catch (IOException e) {
            normalizeDirPath = this.dDirPath;
        }

        //// 初期化
        context.dirPath = normalizeDirPath;
        TextView dirPathTextView = context.dialogDirView.findViewById(R_ID_DIALOG_CURRENT_PATH);
        ListView dirListLv = context.dialogDirView.findViewById(R_ID_DIALOG_FILE_LIST);

        // ----------------------------------
        // メイン処理
        // ----------------------------------
        //// 現在のディレクトリのパスをViewにセット
        dirPathTextView.setText(context.dirPath);

        //// ディレクトリ一覧をViewにセット

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context.getContext(),
                android.R.layout.simple_list_item_1,
                new FileExtended(context.dirPath).listDirFile()   //Sting[]、ファイル一覧
        );
        dirListLv.setAdapter(adapter);
    }

    /************************************
     * 設定の値を保存する、ダイアログが閉じたとき
     * @param positiveResult true:ユーザーがポジティブボタン（OKボタン）を押したとき,
     *                      false:ネガティブボタン、またはキャンセルボタンを押したとき
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
Log.d("○"+this.getClass().getSimpleName(), "onDialogClosed() が呼ばれた: " + positiveResult);
        // OKボタンを押してダイアログを閉じたとき選択ディレクトリパスを保存する
        if (positiveResult) {
            // 設定値を保存
            this.persistString(this.dirPath);
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
Log.d("○"+this.getClass().getSimpleName(), "onSetInitialValue() が呼ばれた: " + restorePersistedValue);
        if ( restorePersistedValue ) {
            this.dirPath = this.getPersistedString(null);
        } else {
            this.persistString( (String)defaultValue );
            this.dirPath = (String) defaultValue;
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
Log.d("○"+this.getClass().getSimpleName(), "onGetDefaultValue() が呼ばれた: " + tArray.getString(index));

        //// 特殊文字の時初期値を加工
        String defaultStr = tArray.getString(index);
        //noinspection ConstantConditions
        switch (defaultStr) {
            case "DCIM":
                this.dDirPath = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .getAbsolutePath()
                        + System.getProperty("file.separator");
                break;
            case "PICTURES":
                this.dDirPath = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath()
                        + System.getProperty("file.separator");
                break;
            default:
                this.dDirPath = defaultStr;
                break;
        }


        return this.dDirPath;
    }

    /******************************
     * 回転直前に値を保存する
     * @return 保存する値
     */
    @Override
    protected Parcelable onSaveInstanceState() {
Log.d("○"+this.getClass().getSimpleName(), "onSaveInstanceState() が呼ばれた: " +this.hashCode());
        return super.onSaveInstanceState();
    }

    /***********************************
     * 回転直後に値を読み出す
     * @param state par
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
Log.d("○"+this.getClass().getSimpleName(), "onRestoreInstanceState() が呼ばれた:"+this.hashCode());
    }

}
