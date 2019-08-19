package xyz.goodistory.autowallpaper.preference

import android.content.Context
import android.content.res.TypedArray
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
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
    private var bucketId: String? = null

    /** XML属性の値 */
    private val dialogCurrentPathId: Int?
    private val dialogFileListId: Int?

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    companion object {
        private const val ALL_BUCKET_DISPLAY_NAME: String = "ALL"

        /**
         * bucket display name → bucket display ids
         */
        private fun toBucketIds(filterBucketDisplayName: String, buckets: Map<Int, String>): Set<Int> {
            return mutableSetOf<Int>().apply {
                for ( (bucketId, bucketDisplayName) in buckets ) {
                    if (filterBucketDisplayName == bucketDisplayName) {
                        add(bucketId)
                    }
                }
            }
        }

        /**
         * bucket display name → bucket display id
         */
        private fun toBucketId(bucketDisplayName: String, buckets: Map<Int, String>): Int {
            val filteredBucketIds: Set<Int> = toBucketIds(bucketDisplayName, buckets)
            return filteredBucketIds.first()
        }

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
     * 保存された値がなくて、mDefaultValue がnullの場合は呼ばれない
     *
     * @param defaultValue 保存された値がない場合: onGetDefaultValue()の戻り値,
     *                      保存された値がある場合: null
     */
    override fun onSetInitialValue(defaultValue: Any?) {
        // 永続化用に値の型を加工
        val defaultBucketId: String? = defaultValue as? String

        // 永続化した値を取得、ない場合はデフォルト値
        val persistedBucketId: String = getPersistedString(defaultBucketId)

        // 取得した値を保存したりプロパティにセットしたり
        setAndPersist(persistedBucketId)
    }

    /**
     * preferences.xml から 親クラスの mDefaultValue にセットするときに呼ばれる
     * コンストラクタでsuper() したときに呼ばれる
     * defaultValue がない場合は呼ばれない
     *
     * @param a <Preference>の属性の全ての配列
     * @param index <Preference>の属性配列に対する「defaultValue」属性のインデックス
     * @return mDefaultValue にセットされる値, onSetInitialValue() に提供される値, bucket_id
     */
    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any? {
        //// 初期処理
        // XMLから値を取得
        val defaultBucketDisplayName: String = a?.getString(index)
                ?: throw IllegalStateException("TypedArray is null!")   // 通常ここには来ない

        //// 特殊処理
        if (defaultBucketDisplayName == ALL_BUCKET_DISPLAY_NAME) {
            return ALL_BUCKET_DISPLAY_NAME  // String
        }

        //// 通常処理
        val displayNames: Map<Int, String> = getImageMediaAllBuckets()
        if ( !displayNames.containsValue(defaultBucketDisplayName) ) {
            throw IllegalArgumentException(
                    "DefaultValue attribute of preferences XML is invalid. " +
                    "Please chose from $displayNames")
        }

        return toBucketId(defaultBucketDisplayName, displayNames).toString()   // String型をreturn
    }


    // --------------------------------------------------------------------
    // 処理まとめてるだけ
    // --------------------------------------------------------------------
    /**
     * フィールドにセット、persist、変更を知らせるを一度にする
     */
    private fun setAndPersist(setBucketId: String) {
        if (bucketId != setBucketId) {
            bucketId = setBucketId
            persistString(setBucketId)
            notifyChanged()
        }
    }

    /**
     * MediaStoreの画像にある、全てののbucket_idとbucket_display_nameの組み合わせを取得
     *
     * MediaStore.Images.ImageColumns.BUCKET_ID は bucket_idのカラム名
     * MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME もカラム名
     *
     * @return bucketDisplayNames[ bucketId ] = bucketDisplayName の Map
     */
    private fun getImageMediaAllBuckets(): Map<Int, String> {
        //// クエリを実行
        // SELECT 句の値
        val projection: Array<String>
                = arrayOf("DISTINCT " + MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
        // クエリ実行
        val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null,null,null)

        //// bucketNamesを取得してreturn, !! はnullのときにnullPointerExceptionを投げる
        return cursor!!.run{
            val bucketDisplayNames: MutableMap<Int, String> = mutableMapOf()

            while( moveToNext() ) {
                // bucket名を取得
                val bucketId: Int = getInt(
                        getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_ID))
                val bucketDisplayName: String = getString(
                        getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
                // リストの最後尾にpush
                bucketDisplayNames[bucketId] = bucketDisplayName
            }
            cursor.close()

            bucketDisplayNames
        }
    }



    /**
     * ディレクトリタイプからディレクトリPathに変換
     */
//    private fun getDirectoryPah(directoryType: String): String {
//        if ( !DIRECTORY_TYPES.keys.contains(directoryType) ) {
//            throw IllegalArgumentException(
//                    "XML defaultValue is only selected from " + DIRECTORY_TYPES.keys.toString())
//        }
//
//
//
//        val directoryPathFile: File = if (directoryType == DIRECTORY_ROOT) {
//            // TODO API29(Q)から非推奨になるのでQがリリースされたらなんとかする
//            Environment.getExternalStorageDirectory()
//        } else {
//            // TODO API29(Q)から非推奨になるのでQがリリースされたらなんとかする
//            Environment.getExternalStoragePublicDirectory(DIRECTORY_TYPES[directoryType])
//        }
//
//        return directoryPathFile.toString()
//    }

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
//
//            val preference: SelectDirectoryPreference = getSelectDirectoryPreference()
//            directoryPath = preference.bucketIds
//                    ?: preference.getDirectoryPah(DIRECTORY_ROOT)
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
            // 変数準備
            // --------------------------------------------------------------------
            val preference: SelectDirectoryPreference = getSelectDirectoryPreference()
            if (view == null) {
                // ここにくることはない
                throw RuntimeException("view of the dialog of directory list is null")
            }

            val dialogCurrentPathId: Int = preference.dialogCurrentPathId ?: throw RuntimeException(
                    "The id of current path in dialog is null. Please set id in preference.")
            val dialogFileListId: Int = preference.dialogFileListId ?: throw RuntimeException(
                    "The id of file list in dialog is null. Please set id in preference.")


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



            val file: File = File(directoryPath)
            val childrenFiles = file.listFiles()


            val paths: List<String> = directoryPath.run {

                listOf("sss", "fff")
            }

            directoryListView.apply {
                val adapter: ArrayAdapter<String>
                        = ArrayAdapter(context, android.R.layout.simple_list_item_1, paths)
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