package xyz.monogatari.autowallpaper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * パーミッション許可を何故求めるかの説明の大ログ
 * Created by k-shunsuke on 2018/03/09.
 */

public class PermissionDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(this.getActivity())
                .setMessage(R.string.permission_toast)
                .setPositiveButton(R.string.util_ok, null)
                .create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ActivityCompat.requestPermissions(
                this.getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                22222); //todo リクエストコードちゃんとする
    }
}
