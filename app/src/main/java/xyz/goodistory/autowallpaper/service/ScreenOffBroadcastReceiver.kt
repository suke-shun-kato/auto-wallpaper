package xyz.goodistory.autowallpaper.service

import android.content.*
import android.preference.PreferenceManager
import xyz.goodistory.autowallpaper.R
import xyz.goodistory.autowallpaper.wpchange.WpManagerService

class ScreenOffBroadcastReceiver : BroadcastReceiver() {

    companion object {
        /**
         * Context側でのregisterReceiver()時の処理をまとめただけ
         */
        @JvmStatic
        fun registerReceiver(context: Context, broadcastReceiver: ScreenOffBroadcastReceiver) {
            val intentFilter = IntentFilter().apply{
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            context.registerReceiver(broadcastReceiver, intentFilter)
            broadcastReceiver.onRegister(context)
        }
    }

    /**
     * ブロードキャスト受信のタイミングで実行されるコールバック
     * ※別スレッドでブロードキャストレシーバーを登録しても、
     * @param context このレシーバーを登録した「アクティビティ」or「サービス」のコンテキスト
     * @param intent ブロードキャスト配信側から送られてきたインテント
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        //// 初期処理、スマートキャスト
        if (context == null) {
            return
        }
        if (intent == null) {
            return
        }

        //// 変更のタイミング -> 画面OFF時 が OFFのとき途中で切り上げ
        val preferenceKey: String = context.getString(R.string.preference_key_when_screen_off)
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if ( !sp.getBoolean(preferenceKey, false) ) {
            return
        }

        //// ACTION が電源OFF以外のとき途中で切り上げ
        if ( intent.action == null || intent.action != Intent.ACTION_SCREEN_OFF ) {
            return
        }

        //// 壁紙変更実行
        val screenOffHistoryModel = ScreenOffHistoriesModel(context)
        val intervalCount: Long = sp.getString(
                context.getString(R.string.preference_key_when_screen_off_count), "")!!
                .toLong()
        // TODO ここおちる
        if ( screenOffHistoryModel.getCount() % intervalCount == intervalCount - 1 ) {  // 10は暫定
            // count
            WpManagerService.changeWpRandom(context)
        }

        // TODO db.close() のタイミングをちゃんとする
        screenOffHistoryModel.countUp()

    }

    /**
     * Contextでregisterのときにこれ呼ぶようにする
     */
    fun onRegister(context: Context) {
        ScreenOffHistoriesModel(context).reset()
    }
}