package xyz.goodistory.autowallpaper.preference

import android.content.Context
import android.content.res.TypedArray
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import xyz.goodistory.autowallpaper.R
import android.widget.RadioButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


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
        dialogCurrentBucketId = attributes["dialogCurrentBucketId"]
        dialogFileListId = attributes["dialogFileListId"]
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        val attributes: Map<String, Int> = getCustomAttributes(context, attrs, defStyleAttr)
        dialogCurrentBucketId = attributes["dialogCurrentBucketId"]
        dialogFileListId = attributes["dialogFileListId"]

    }
    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs) {
        val attributes: Map<String, Int> = getCustomAttributes(context, attrs)
        dialogCurrentBucketId = attributes["dialogCurrentBucketId"]
        dialogFileListId = attributes["dialogFileListId"]
    }

    constructor(context: Context): super(context) {
        // TODO ここ例外を投げたほうがいいか考える
        dialogCurrentBucketId = null
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
                    "dialogCurrentBucketId" to typedArray.getResourceId(
                            R.styleable.SelectDirectoryPreference_dialogCurrentBucketId, 0),
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
    private val dialogCurrentBucketId: Int?
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




        private fun getThumbnails(context: Context): List<Bitmap> {
            // cursor取得
            val cursor: Cursor? = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null)

            // サムネイル取得
            val bitmapThumbnails = cursor!!.run {
                val bitmaps: MutableList<Bitmap> = mutableListOf()

                while(moveToNext()) {

                    val id: Long = getLong( getColumnIndex(MediaStore.Images.ImageColumns._ID) )
                    val bitmap: Bitmap? = MediaStore.Images.Thumbnails.getThumbnail( context.contentResolver,
                            id, MediaStore.Images.Thumbnails.MICRO_KIND, null)
                    if (bitmap != null ) {
                        bitmaps.add(bitmap)
                    }
                }

                bitmaps
            }

            cursor.close()
            return bitmapThumbnails
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
        val allBuckets = cursor!!.run{
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

            bucketDisplayNames
        }

        cursor.close()

        return allBuckets
    }


    /**
     * bucketIdからbucketDisplayNameを取得
     * @return bucketDisplayName,ない場合はnull
     */
    private fun toBucketDisplayName(bucketId: Int): String? {
        val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Images.ImageColumns.BUCKET_ID + " = ?",
                arrayOf(bucketId.toString()),
                null, null)

        val displayName = cursor!!.run {
            // 先頭に移動
            val canMove: Boolean = moveToFirst()
            if (canMove) {
                // コンテントプロバイダから値を取得
                getString(getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
            } else {
                null
            }
        }

        cursor.close()

        return displayName
    }

    private fun toBucketDisplayName(bucketId: String): String? {
        return if (bucketId == ALL_BUCKET_DISPLAY_NAME) {
            ""
        } else {
            toBucketDisplayName(bucketId.toInt())
        }
    }

    // --------------------------------------------------------------------
    // class
    // --------------------------------------------------------------------
    class BucketModel(val bucketId: Int, val bucketDisplayName: String) {
        companion object {
            fun createList(buckets: Map<Int, String>): List<BucketModel> {
                val list: MutableList<BucketModel> = mutableListOf()

                buckets.forEach{ (bucketId, bucketDisplayName) ->
                    list.add( BucketModel(bucketId, bucketDisplayName) )
                }
                return list
            }
        }
    }

    class Adapter : ListAdapter<BucketModel, Adapter.ViewHolder>(DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val itemView = LayoutInflater.from(parent.context)
                    // TODO Rをxmlから取得したい
                    .inflate(R.layout.dialog_fragment_select_directory_preference_item,
                            parent, false)

            return ViewHolder(itemView )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemModel: BucketModel = getItem(position)
            holder.bind(itemModel)
        }


        // ------------------------------
        // class
        // ------------------------------
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            /**
             * バインド時の処理、表示の設定をここに書く
             */
            fun bind(bucketModel: BucketModel) {
                itemView.apply {
                    // TODO Rをxmlから取得したい
                    val radioButton: RadioButton =  findViewById(R.id.item_bucket_display_name)
                    radioButton.text = bucketModel.bucketDisplayName
                }
            }
        }

        class DiffCallback: DiffUtil.ItemCallback<BucketModel>() {
            override fun areItemsTheSame(oldItem: BucketModel, newItem:BucketModel): Boolean {
                return oldItem.bucketId == newItem.bucketId
            }

            override fun areContentsTheSame(oldItem: BucketModel, newItem: BucketModel): Boolean {
                return oldItem.bucketId == newItem.bucketId
                        && oldItem.bucketDisplayName == newItem.bucketDisplayName
            }
        }
    }



    // --------------------------------------------------------------------
    // class
    // --------------------------------------------------------------------
    class Dialog: PreferenceDialogFragmentCompat() {
        private lateinit var selectedBucketId: String
        private lateinit var bucketModels: List<BucketModel>

        private lateinit var recyclerView: RecyclerView
        private lateinit var viewAdapter: Adapter
        private lateinit var viewManager: RecyclerView.LayoutManager

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

        // --------------------------------------------------------------------
        // override
        // --------------------------------------------------------------------
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val preference: SelectDirectoryPreference = getSelectDirectoryPreference()

            //// フィールドにセット
            bucketModels = BucketModel.createList(preference.getImageMediaAllBuckets())
            selectedBucketId = preference.bucketId ?: ALL_BUCKET_DISPLAY_NAME
        }


        /**
         * Binds views in the content View of the dialog to data.
         * Make sure to call through to the superclass implementation.
         *
         * @param view The content View of the dialog, if it is custom.
         */
        override fun onBindDialogView(view: View?) {
            super.onBindDialogView(view)

            // ---------------------------------
            // 変数準備
            // ---------------------------------
            if (view == null) {
                // ここにくることはない
                throw RuntimeException("view of the dialog of directory list is null")
            }
            if (context == null) {
                throw RuntimeException("context is null")
            }

            val preference: SelectDirectoryPreference = getSelectDirectoryPreference()

            // ---------------------------------
            //
            // ---------------------------------
            val dialogCurrentBucketId: Int = preference.dialogCurrentBucketId ?: throw RuntimeException(
                    "The id of current path in dialog is null. Please set id in preference.")
            val currentBucketTextView: TextView = view.findViewById(dialogCurrentBucketId)
            currentBucketTextView.text = preference.toBucketDisplayName(selectedBucketId)

            // ---------------------------------
            //
            // ---------------------------------
            val dialogFileListId: Int = preference.dialogFileListId ?: throw RuntimeException(
                    "The id of file list in dialog is null. Please set id in preference.")

            recyclerView = view.findViewById(dialogFileListId)
            viewManager = LinearLayoutManager(context)
            viewAdapter = Adapter().apply {
                submitList(bucketModels)
            }

            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            if (positiveResult) {
                getSelectDirectoryPreference().setAndPersist(selectedBucketId)
            }
        }

        // --------------------------------------------------------------------
        // 処理をまとめただけ
        // --------------------------------------------------------------------
        private fun getSelectDirectoryPreference(): SelectDirectoryPreference {
            return preference as SelectDirectoryPreference

        }


    }
}