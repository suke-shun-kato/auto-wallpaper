package xyz.goodistory.autowallpaper.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import xyz.goodistory.autowallpaper.R
import java.io.File

/**
 * ディレクトリ選択ダイアログのプリファレンス
 */
class SelectDirectoryPreference : DialogPreference {
    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {

        val attributes: Map<String, Int> = getCustomAttributes(context, attrs, defStyleAttr, defStyleRes)
        dialogCurrentPathId = attributes["dialogCurrentPathId"]
        dialogFileListId = attributes["dialogFileListId"]
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        val attributes: Map<String, Int> = getCustomAttributes(context, attrs, defStyleAttr)
        dialogCurrentPathId = attributes["dialogCurrentPathId"]
        dialogFileListId = attributes["dialogFileListId"]

    }
    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs) {
        val attributes: Map<String, Int> = getCustomAttributes(context, attrs)
        dialogCurrentPathId = attributes["dialogCurrentPathId"]
        dialogFileListId = attributes["dialogFileListId"]
    }

    constructor(context: Context): super(context) {
        // TODO ここ例外を投げたほうがいいか考える
        dialogCurrentPathId = null
        dialogFileListId = null
    }

    private fun getCustomAttributes(
            context: Context, attrs: AttributeSet, defStyleAttr: Int = 0, defStyleRes: Int = 0)
            : Map<String, Int> {

        val typedArray: TypedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.SelectDirectoryPreference, defStyleAttr, defStyleRes)

        val attributeValues: Map<String, Int>
        try {
            attributeValues = mapOf(
                    "dialogCurrentPathId" to typedArray.getResourceId(
                            R.styleable.SelectDirectoryPreference_dialogCurrentPathId, 0),
                    "dialogFileListId" to typedArray.getResourceId(
                            R.styleable.SelectDirectoryPreference_dialogFileListId, 0))
            // TODO 例外の投げ方
//        } catch (e: Exception) {
//            throw e
        } finally {
            typedArray.recycle()
        }

        return attributeValues
    }

    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** ディレクトリパス、XMLのdefaultValueがなくて永続化してる値がないときnull */
    private var directoryPath: String? = null

    /** XML属性の値*/
    private val dialogCurrentPathId: Int?
    private val dialogFileListId: Int?

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    companion object {
        // TODO これ列挙型とかで別の書き方あるかも
        private const val DIRECTORY_MUSIC = "MUSIC"
        private const val DIRECTORY_RINGTONES = "RINGTONES"
        private const val DIRECTORY_ALARMS = "ALARMS"
        private const val DIRECTORY_NOTIFICATIONS = "NOTIFICATIONS"
        private const val DIRECTORY_PICTURES = "PICTURES"
        private const val DIRECTORY_MOVIES = "MOVIES"
        private const val DIRECTORY_DOWNLOADS = "MOVIES"
        private const val DIRECTORY_DCIM = "DCIM"
        private const val DIRECTORY_DOCUMENTS = "DOCUMENTS"
        private const val DIRECTORY_ROOT = "ROOT"

        /** Environment.getExternalStoragePublicDirectory()の引数へ変換表 */
        private val DIRECTORY_TYPES: Map<String, String?> = mapOf(
                DIRECTORY_MUSIC to Environment.DIRECTORY_MUSIC,
                DIRECTORY_MUSIC to Environment.DIRECTORY_PODCASTS,

                DIRECTORY_RINGTONES to Environment.DIRECTORY_RINGTONES,
                DIRECTORY_ALARMS to Environment.DIRECTORY_ALARMS,

                DIRECTORY_NOTIFICATIONS to Environment.DIRECTORY_NOTIFICATIONS,
                DIRECTORY_PICTURES to Environment.DIRECTORY_PICTURES,

                DIRECTORY_MOVIES to Environment.DIRECTORY_MOVIES,
                DIRECTORY_DOWNLOADS to Environment.DIRECTORY_DOWNLOADS,

                DIRECTORY_DCIM to Environment.DIRECTORY_DCIM,
                DIRECTORY_DOCUMENTS to Environment.DIRECTORY_DOCUMENTS,

                DIRECTORY_ROOT to null)
    }

    // --------------------------------------------------------------------
    // override
    // --------------------------------------------------------------------
    override fun onClick() {
        super.onClick()

        // TODO ここにパーミッション許可ダイアログの処理を書く
    }


    /**
     * コンストラクタの処理終了の後に呼ばれる、設定画面が表示された瞬間に呼ばれる
     *
     * @param defaultValue 保存された値がない場合: onGetDefaultValue()の戻り値,
     *                                            XMLにdefaultValueがない場合は呼ばれない
     *                      保存された値がある場合: null
     */
    override fun onSetInitialValue(defaultValue: Any?) {
        val defaultValueStr: String = if (defaultValue == null) {
            getDirectoryPah(DIRECTORY_ROOT)
        } else {
            defaultValue as String
        }

        setAndPersistDirectoryPath( getPersistedString(defaultValueStr) )
    }

    /**
     * preferences.xml から 親クラスの mDefaultValue にセットするときに呼ばれる
     * コンストラクタでsuper() したときに呼ばれる
     * defaultValue がない場合は呼ばれない
     *
     * @param a <Preference>の属性の全ての配列
     * @param index <Preference>の属性配列に対する「defaultValue」属性のインデックス
     * @return mDefaultValue にセットされる値, onSetInitialValue() に提供される値
     */
    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any? {
        //// 初期処理
        // XMLから値を取得
        val xmlDefaultValue: String = a?.getString(index)
                ?: throw IllegalStateException("TypedArray is null!")   // 通常ここには来ない

        return getDirectoryPah(xmlDefaultValue)
    }


    // --------------------------------------------------------------------
    // 処理まとめてるだけ
    // --------------------------------------------------------------------
    /**
     * フィールドにセット、persist、変更を知らせるを一度にする
     * @param setDirectoryPath setしたいディレクトリのPath
     */
    fun setAndPersistDirectoryPath(setDirectoryPath: String) {
        if ( directoryPath != setDirectoryPath ) {
            directoryPath = setDirectoryPath
            persistString(setDirectoryPath)
            notifyChanged()
        }
    }


    /**
     * ディレクトリタイプからディレクトリPathに変換
     */
    private fun getDirectoryPah(directoryType: String): String {
        if ( !DIRECTORY_TYPES.keys.contains(directoryType) ) {
            throw IllegalArgumentException(
                    "XML defaultValue is only selected from " + DIRECTORY_TYPES.keys.toString())
        }

        val directoryPathFile: File = if (directoryType == DIRECTORY_ROOT) {
            // TODO API29(Q)から非推奨になるのでQがリリースされたらなんとかする
            Environment.getExternalStorageDirectory()
        } else {
            // TODO API29(Q)から非推奨になるのでQがリリースされたらなんとかする
            Environment.getExternalStoragePublicDirectory(DIRECTORY_TYPES[directoryType])
        }

        return directoryPathFile.toString()
    }

    // --------------------------------------------------------------------
    // class
    // --------------------------------------------------------------------
    class Dialog: PreferenceDialogFragmentCompat() {
        private lateinit var directoryPath: String

        // --------------------------------------------------------------------
        // override
        // --------------------------------------------------------------------
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val preference: SelectDirectoryPreference = getSelectDirectoryPreference()
            directoryPath = preference.directoryPath
                    ?: preference.getDirectoryPah(DIRECTORY_ROOT)
        }


        /**
         * Binds views in the content View of the dialog to data.
         * Make sure to call through to the superclass implementation.
         *
         * @param view The content View of the dialog, if it is custom.
         */
        override fun onBindDialogView(view: View?) {
            super.onBindDialogView(view)

            // --------------------------------------------------------------------
            //
            // --------------------------------------------------------------------
            val preference: SelectDirectoryPreference = getSelectDirectoryPreference()
            if (view == null) {
                throw RuntimeException("fff")
            }



            val dialogCurrentPathId: Int = preference.dialogCurrentPathId
                    ?: throw RuntimeException("sss")
            val dialogFileListId: Int = preference.dialogFileListId
                    ?: throw RuntimeException("sss")


            // TODO フィールド化?
            val currentDirectoryTextView = view.findViewById<TextView>(dialogCurrentPathId)
            val directoryListView = view.findViewById<ListView>(dialogFileListId)


            // --------------------------------------------------------------------
            //
            // --------------------------------------------------------------------
            // ディレクトリをセット、リスナーをセット
            currentDirectoryTextView.apply {
                text = directoryPath
            }



            directoryListView.apply {
                val adapter: ArrayAdapter<String> = ArrayAdapter(context, android.R.layout.simple_list_item_1, listOf("ssss", "fffff", "iiiii"))
                setAdapter(adapter)
            }

        }

        override fun onDialogClosed(positiveResult: Boolean) {
            Log.d("ssss", "fffff")
//            TODO("not implemented")
        }

        // --------------------------------------------------------------------
        //
        // --------------------------------------------------------------------
        private fun getSelectDirectoryPreference(): SelectDirectoryPreference {
            return preference as SelectDirectoryPreference

        }

        // --------------------------------------------------------------------
        //
        // --------------------------------------------------------------------
        companion object {

            fun newInstance(key: String): Dialog {
                return Dialog().apply {
                    arguments = Bundle(1).apply {
                        putString(ARG_KEY, key)
                    }
                }
            }

        }
    }
}