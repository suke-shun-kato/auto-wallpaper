package xyz.goodistory.autowallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import xyz.goodistory.autowallpaper.wpchange.WpManagerService;

/**
 * share で画像が送られてきたときのダイアログ、OKボタンを押すと壁紙をセットする
 */
public class ShareImageFragment extends DialogFragment {
    public static final String TAG = ShareImageFragment.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // ----------------------------------
        // エラー処理
        // ----------------------------------
        final Activity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException("Activity is null!");
        }

        final Intent i = activity.getIntent();
        if (i == null) {
            throw new IllegalStateException("Intent is null!");
        }

        final String action = i.getAction();
        if ( action == null || !action.equals(Intent.ACTION_SEND) ) {
            throw new IllegalStateException("Action must be ACTION_SEND!");
        }

        final String type = i.getType();
        if ( type == null || !type.contains("image/") ) {
            throw new IllegalStateException("Type must be image/*");
        }

        final Bundle bundle = i.getExtras();
        if (bundle == null) {
            throw new IllegalStateException("Bundle is null!");
        }

        final Object uriStr = bundle.get(Intent.EXTRA_STREAM);
        if (uriStr == null) {
            throw new IllegalStateException("EXTRA_STREAM is null!");
        }

        // ----------------------------------
        // builder で ダイアログの中身を作成
        // ----------------------------------
        AlertDialog.Builder builder =  new AlertDialog.Builder(activity)
            .setTitle(R.string.main_share_dialog_title)
            .setMessage(R.string.main_share_dialog_message)
            .setPositiveButton(R.string.util_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    Uri uri = Uri.parse(uriStr.toString());

                    WpManagerService.changeWpSpecified(activity, uriStr.toString(), HistoryModel.SOURCE_SHARE, uriStr.toString());

Log.d("onCreateDialog", "action:" + action + ",\n type:" + type + ",\n uri:" + i.getDataString() + ",\n Bundle:" + uriStr.toString());
                }
            })
            .setNegativeButton(R.string.util_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 何もしない
                }
            });

        return builder.show();
    }
}
