package xyz.goodistory.autowallpaper.preference

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import xyz.goodistory.autowallpaper.R
import android.widget.RadioButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


/**
 * ディレクトリ選択ダイアログのプリファレンス
 */
class SelectImageBucketPreference : DialogPreference {
    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        dialogListItemRLayout = getCustomAttribute(context, attrs, defStyleAttr, defStyleRes)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        dialogListItemRLayout = getCustomAttribute(context, attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs) {
        dialogListItemRLayout = getCustomAttribute(context, attrs)
    }

    private fun getCustomAttribute(
            context: Context, attrs: AttributeSet, defStyleAttr: Int = 0, defStyleRes: Int = 0)
            : Int {
        val typedArray: TypedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.SelectImageBucketPreference, defStyleAttr, defStyleRes)

        val dialogListItemRLayout: Int
        try {
            dialogListItemRLayout = typedArray.getResourceId(
                            R.styleable.SelectImageBucketPreference_dialogListItemLayout, 0)
        } finally {
            typedArray.recycle()
        }

        return dialogListItemRLayout
    }

    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** ディレクトリパス、XMLのdefaultValueがなくて永続化してる値がないときnull */
    private var bucketId: Int? = null

    /** パーミッション許可のダイアログを表示するときのRequestCode、nullのときはダイアログを表示しない */
    private var requestCodePermissionDialog: Int? = null
    private var activity: FragmentActivity? = null
    private var permissionRationaleDialogText: String? = null

    /** XML属性の値 */
    private val dialogListItemRLayout: Int?

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    companion object {
        private const val ALL_BUCKET_ID: Int = 0
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

        /**
         * bucketIDを指定して画像のid複数を取得
         */
        @JvmStatic
        fun getImageIdsFromSharedPreferences(
                sp: SharedPreferences, preferenceKey: String, cr: ContentResolver): List<Long> {

            //// spから取得
            val bucketId = sp.getInt(preferenceKey, ALL_BUCKET_ID)


            //// query実行
            val projection: Array<String> = arrayOf(MediaStore.Images.ImageColumns._ID)
            val selection: String?
            val selectionArgs: Array<String>?
            if (bucketId == ALL_BUCKET_ID) {
                selection = null
                selectionArgs = null
            } else {
                selection = MediaStore.Images.ImageColumns.BUCKET_ID + " = ?"
                selectionArgs = arrayOf(bucketId.toString())
            }
            val cursor: Cursor? = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, selection,selectionArgs, null)

            //// _idを抜き出す
            val imageIds = cursor!!.run {
                val mutableImageIds: MutableList<Long> = mutableListOf()
                while (moveToNext()) {
                    val id: Long = getLong(getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
                    mutableImageIds.add(id)
                }

                mutableImageIds
            }

            //// 後始末
            cursor.close()

            return imageIds
        }

        /**
         * bucketIDを指定して画像のuri複数を取得
         */
        @JvmStatic
        fun getUrisFromSharedPreferences(
                sp: SharedPreferences, preferenceKey: String, cr: ContentResolver): List<Uri> {

            // SharedPreference からbucketId を取得して 画像のidを複数取得
            val imageIds: List<Long> = getImageIdsFromSharedPreferences(sp, preferenceKey, cr)

            // Uriに変換
            return mutableListOf<Uri>().apply{
                imageIds.forEach{imageId: Long ->
                    val uri: Uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId)
                    add(uri)
                }
            }
        }
    }

    // --------------------------------------------------------------------
    // override
    // --------------------------------------------------------------------
    override fun onClick() {
        //// パーミッション許可ダイアログの表示設定がされていない場合は、super()実行して終わり
        if (requestCodePermissionDialog == null || activity == null) {
            super.onClick()     //ここでonCreateDialogView()が呼ばれる
            return
        }

        //// 既にパーミッション許可されている場合は、super()実行して終わり
        val permission: String = Manifest.permission.READ_EXTERNAL_STORAGE
        val permissionStatus = ContextCompat.checkSelfPermission(context, permission)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
        // パーミッション許可されている場合
            super.onClick()     //ここでonCreateDialogView()が呼ばれる
            return
        }

        //// 許可されていない場合
        val shouldShowRationale: Boolean            // Rationale: 根拠
                = ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)
        if (shouldShowRationale) {
            val bundle: Bundle = Bundle().apply {
                putString(RationaleDialogFragment.BUNDLE_KEY_TEXT, permissionRationaleDialogText)
                putInt(RationaleDialogFragment.BUNDLE_KEY_DIALOG_REQUEST_CODE, requestCodePermissionDialog!!)
            }

            RationaleDialogFragment().apply{
                arguments = bundle
            }.show(activity!!.supportFragmentManager, RationaleDialogFragment.FRAGMENT_TAG_NAME)

        } else {
        // 説明理由の表示が必要でない場合、初回など
            ActivityCompat.requestPermissions(
                    activity!!, arrayOf(permission), requestCodePermissionDialog!!)
        }
    }

    /**
     * パーミッション許可ダイアログで拒否したときに表示するダイアログ
     */
    class RationaleDialogFragment: DialogFragment() {
        // TODO FRAGMENT_TAG_NAME をちゃんとする
        companion object {
            const val FRAGMENT_TAG_NAME = "ffffff"
//            val FRAGMENT_TAG_NAME: String = RationaleDialogFragment::class.simpleName
            const val BUNDLE_KEY_TEXT = "text"
            const val BUNDLE_KEY_DIALOG_REQUEST_CODE = "permission_code"
        }

   
        override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
            return AlertDialog.Builder(activity)
                    .setMessage(arguments!!.getString(BUNDLE_KEY_TEXT))
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                        ActivityCompat.requestPermissions(
                                activity!!,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                arguments!!.getInt(BUNDLE_KEY_DIALOG_REQUEST_CODE) )
                    }.create()
        }
    }

    /**
     * コンストラクタの処理終了の後に呼ばれる、設定画面が表示された瞬間に呼ばれる
     * 保存された値がなくて、mDefaultValue がnullの場合は呼ばれない
     *
     * @param defaultValue 永続化された値がない場合: onGetDefaultValue()の戻り値,
     *                      永続化された値がある場合: null
     */
    override fun onSetInitialValue(defaultValue: Any?) {
        // 永続化した値を取得、ない場合はデフォルト値
        val persistedBucketId: Int = if (defaultValue == null) {
        // 永続化した値がある場合
            getPersistedInt(0)  // ここの0はセットされない
        } else {
        // 永続化した値がない場合
            defaultValue as Int
        }

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
            return ALL_BUCKET_ID
        }

        //// 通常処理
        val displayNames: Map<Int, String> = getImageMediaAllBuckets()
        if ( !displayNames.containsValue(defaultBucketDisplayName) ) {
            throw IllegalArgumentException(
                    "DefaultValue attribute of preferences XML is invalid. " +
                    "Please chose from $displayNames")
        }

        return toBucketId(defaultBucketDisplayName, displayNames)
    }

    // --------------------------------------------------------------------
    // 外から使う
    // --------------------------------------------------------------------
    fun setBucketToSummary() {
        val bucketId: Int = getPersistedInt(0)
        summary = toBucketDisplayName(bucketId)
    }

    fun setShowRequestPermissionDialog(
            setActivity: FragmentActivity, setRequestCode: Int, text: String) {

        activity = setActivity
        requestCodePermissionDialog = setRequestCode
        permissionRationaleDialogText = text
    }

    fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        if ( permissions.size == 1
                && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE
                && grantResults.size == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {  // パーミッション許可ダイアログでOKを押されたとき
            onClick()
        }
    }

    // --------------------------------------------------------------------
    // 処理まとめてるだけ
    // --------------------------------------------------------------------
    /**
     * フィールドにセット、persist、変更を知らせるを一度にする
     */
    private fun setAndPersist(setBucketId: Int) {
        if (bucketId != setBucketId) {
            bucketId = setBucketId
            persistInt(setBucketId)
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
     * @return bucketDisplayName, ない場合はnull
     */
    private fun toBucketDisplayName(bucketId: Int): String? {
        //// ALL のときだけ特殊処理
        if (bucketId == ALL_BUCKET_ID) {
            return ALL_BUCKET_DISPLAY_NAME
        }

        //// 通常処理
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



    // --------------------------------------------------------------------
    // class
    // --------------------------------------------------------------------
    class ImageBucketModel(var imageIds: MutableList<Long>, var bucketId: Int, var bucketDisplayName: String) {

        companion object {
            fun createList(context: Context): List<ImageBucketModel> {
                val cursor: Cursor? = context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null)

                val models: List<ImageBucketModel> = cursor!!.run {
                    val models: MutableMap<Int, ImageBucketModel> = mutableMapOf()

                    //// ALL の処理
                    moveToPosition(-1)
                    models[ALL_BUCKET_ID] = ImageBucketModel(
                            mutableListOf(), ALL_BUCKET_ID, ALL_BUCKET_DISPLAY_NAME)
                    while (moveToNext()) {
                        val id: Long = getLong(
                                getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID) )
                        models[ALL_BUCKET_ID]!!.imageIds.add(id)
                    }

                    //// 各Bucketの処理
                    moveToPosition(-1)
                    while (moveToNext()) {
                        val id: Long = getLong(
                                getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID) )
                        val bucketId: Int = getInt(getColumnIndexOrThrow(
                                MediaStore.Images.ImageColumns.BUCKET_ID) )
                        val bucketDisplayName: String = getString(getColumnIndexOrThrow(
                                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME) )


                        if ( models.containsKey(bucketId) ) {
                            models[bucketId]!!.imageIds.add(id)
                        } else {
                            models[bucketId] = ImageBucketModel(
                                    mutableListOf(id), bucketId, bucketDisplayName )
                        }
                    }

                    //// MapのvalueをListに変換
                    models.values.toList()
                }

                cursor.close()
                return models
            }
        }
    }

    // --------------------------------------------------------------------
    // class
    // --------------------------------------------------------------------
    /**
     * @param selectedBucketId 選択中のbucketID、初期値
     * @param dialogListItemLayout
     */
    class BucketListAdapter(var selectedBucketId: Int, private val dialogListItemLayout: Int)
        : ListAdapter<ImageBucketModel, BucketListAdapter.ViewHolder>(DiffCallback()) {

        private var beforeCheckedButton: RadioButton? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val itemView = LayoutInflater.from(parent.context)
                    .inflate(dialogListItemLayout, parent, false)

            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemModel: ImageBucketModel = getItem(position)
            holder.bind(itemModel)
        }


        // ------------------------------
        // class
        // ------------------------------
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            /**
             * AdapterでのViewHolderバインド時の処理をまとめているだけ
             */
            fun bind(bucketModel: ImageBucketModel) {
                //// スクロールの中をクリックしたらラジオボタンをクリックしたことにする
                itemView.findViewById<LinearLayout>(R.id.dialog_list_item_thumbnails)
                        .setOnClickListener {
                            itemView.findViewById<RadioButton>(R.id.dialog_list_item_radio).apply {
                                isChecked = true
                            }
                        }

                //// ラジオボタンの設定をする
                itemView.findViewById<RadioButton>(R.id.dialog_list_item_radio).apply {

                    // 文章の設定
                    text = bucketModel.bucketDisplayName

                    // クリックしたとき、以前クリックしたボタンを解除するリスナーをセット
                    setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                        if (isChecked) {
                            beforeCheckedButton?.isChecked = false
                            beforeCheckedButton = buttonView as RadioButton
                            selectedBucketId = bucketModel.bucketId
                        }
                    }

                    // 選択中のbucketIdのラジオボタンだとクリックしたことにする（初期値の設定）
                    if (selectedBucketId == bucketModel.bucketId) {
                        performClick()
                    }
                }

                //// サムネ画像の設定   // TODO ちゃんとする
                itemView.findViewById<ImageView>(R.id.dialog_list_item_thumbnail_1).apply {
                    val index: Int = 0
                    if (bucketModel.imageIds.size > index) {
                        val id: Long = bucketModel.imageIds[index]
                        val bitmap: Bitmap? = MediaStore.Images.Thumbnails.getThumbnail(
                                context.contentResolver,
                                id,
                                MediaStore.Images.Thumbnails.MINI_KIND,
                                null)
                        if (bitmap != null) {
                            setImageBitmap(bitmap)
                        }
                    }
                }
                itemView.findViewById<ImageView>(R.id.dialog_list_item_thumbnail_2).apply {
                    val index: Int = 1
                    if (bucketModel.imageIds.size > index) {
                        val id: Long = bucketModel.imageIds[index]
                        val bitmap: Bitmap? = MediaStore.Images.Thumbnails.getThumbnail(
                                context.contentResolver,
                                id,
                                MediaStore.Images.Thumbnails.MINI_KIND,
                                null)
                        if (bitmap != null) {
                            setImageBitmap(bitmap)
                        }
                    }
                }
                itemView.findViewById<ImageView>(R.id.dialog_list_item_thumbnail_3).apply {
                    val index: Int = 2
                    if (bucketModel.imageIds.size > index) {
                        val id: Long = bucketModel.imageIds[index]
                        val bitmap: Bitmap? = MediaStore.Images.Thumbnails.getThumbnail(
                                context.contentResolver,
                                id,
                                MediaStore.Images.Thumbnails.MINI_KIND,
                                null)
                        if (bitmap != null) {
                            setImageBitmap(bitmap)
                        }
                    }
                }
            }
        }

        class DiffCallback: DiffUtil.ItemCallback<ImageBucketModel>() {
            override fun areItemsTheSame(oldItem: ImageBucketModel, newItem:ImageBucketModel): Boolean {
                return oldItem.bucketId == newItem.bucketId
            }

            override fun areContentsTheSame(oldItem: ImageBucketModel, newItem: ImageBucketModel): Boolean {
                return oldItem.bucketId == newItem.bucketId
                        && oldItem.bucketDisplayName == newItem.bucketDisplayName
            }
        }
    }



    // --------------------------------------------------------------------
    // class
    // --------------------------------------------------------------------
    class Dialog: PreferenceDialogFragmentCompat() {
        private lateinit var bucketModels: List<ImageBucketModel>

        private lateinit var recyclerView: RecyclerView
        private lateinit var viewAdapter: BucketListAdapter

        // --------------------------------------------------------------------
        // companion
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

            // フィールドにセット
            bucketModels = ImageBucketModel.createList(context!!)
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

            val preference: SelectImageBucketPreference = getSelectImageBucketPreference()

            val currentBucketId = preference.bucketId ?: ALL_BUCKET_ID

            // ---------------------------------
            // RecyclerViewのリストを設定
            // ---------------------------------
            viewAdapter = BucketListAdapter(currentBucketId, preference.dialogListItemRLayout!!).apply {
                // リストの値を送信
                submitList(bucketModels)
            }

            recyclerView = (view as RecyclerView).apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = viewAdapter
            }
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            // OKボタンを押したとき値を保存
            if (positiveResult) {
                getSelectImageBucketPreference().setAndPersist(viewAdapter.selectedBucketId)
            }
        }

        // --------------------------------------------------------------------
        // 処理をまとめただけ
        // --------------------------------------------------------------------
        private fun getSelectImageBucketPreference(): SelectImageBucketPreference {
            return preference as SelectImageBucketPreference

        }


    }
}