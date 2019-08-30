package xyz.goodistory.autowallpaper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import xyz.goodistory.autowallpaper.R
import xyz.goodistory.autowallpaper.wpchange.WpManagerService

class ScreenOffBroadcastReceiver : BroadcastReceiver() {
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
        // TODO DBの値をインクリメント
        WpManagerService.changeWpRandom(context)

    }
}