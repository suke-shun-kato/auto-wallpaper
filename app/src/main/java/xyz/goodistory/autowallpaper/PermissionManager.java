package xyz.goodistory.autowallpaper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * パーミッション許可ダイアログ
 * （注意）ダイアログ消した後の挙動は呼び出したActivity側で
 * 　OnRequestPermissionsResultCallback.onRequestPermissionsResult()　をオーバーライドする
 * Created by k-shunsuke on 2018/03/10.
 */
@SuppressWarnings("WeakerAccess")
public class PermissionManager {
    private static final String ARG_KEY = "requestCode";

    /**
     * パーミッション許可を何故求めるかの説明のダイアログ
     * Created by k-shunsuke on 2018/03/09.
     */
    public static class PermissionDialogFragment extends DialogFragment {
        public static final String TAG_NAME = "permission_explain";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(this.getActivity())
                    .setMessage(R.string.reason_read_external_storage_permission_need)
                    .setPositiveButton(R.string.util_ok, null)
                    .create();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();

            ActivityCompat.requestPermissions(
                    this.getActivity(),
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    this.getArguments().getInt(PermissionManager.ARG_KEY));
        }
    }

    /************************************
     * 本体、とりあえずストレージの読み書きを両方許可リクエストを送る
     * @param activity ダイアログを表示するアクティビティ
     * @param requestCode ダイアログ閉じたあとの動作を制御するための値、コールバック制御用
     */
    public static void showRequestDialog(Activity activity, int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            DialogFragment dialog = new PermissionDialogFragment();

            //// 変数入れる
            Bundle args = new Bundle();
            args.putInt(ARG_KEY, requestCode);
            dialog.setArguments(args);

            //// ダイアログを表示
            // tag はFragment自身のタグ、
            // FragmentManager.findFragmentByTag(String tag)でFragmentインスタンスを取得できる
            dialog.show(activity.getFragmentManager(), PermissionDialogFragment.TAG_NAME);

        } else {
            // パーミッション許可ダイアログを表示
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    requestCode
            );
        }
    }
}
