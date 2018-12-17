package xyz.goodistory.autowallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

/**
 * share で画像が送られてきたときのダイアログ、OKボタンを押すと壁紙をセットする
 */
public class ShareImageFragment extends DialogFragment {
    public static final String TAG = ShareImageFragment.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException("activity is null!");
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.main_share_dialog_title)
                .setMessage(R.string.main_share_dialog_message)
                .setPositiveButton(R.string.util_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "OOOOOO", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.util_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 何もしない
                    }
                })
                .show();
    }
}
