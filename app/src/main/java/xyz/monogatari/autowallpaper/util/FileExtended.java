package xyz.monogatari.autowallpaper.util;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 拡張Fileクラス、ファイル＆ディレクトリ一覧取得部分を改造
 * Created by k-shunsuke on 2017/12/09.
 */
public class FileExtended extends File {
    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public FileExtended(File parent, String child) {
        super(parent, child);
    }
    public FileExtended(String pathname) {
        super(pathname);
    }
    public FileExtended(String parent, String child) {
        super(parent, child);
    }
    public FileExtended(URI uri) {
        super(uri);
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * ファイルのパス一覧を取得
     * ディレクトリは最後に/を付ける
     * 親ディレクトリのパス「../」も入れる
     * ディレクトリ→ファイルの順番に並んでいる
     * @return ディレクトリ＆ファイル一覧, this がファイルのときはnullを返す
     */
    public List<String> listDirFile() {
        // ----------------------------------
        // 初期処理
        // ----------------------------------
        //ディレクトリとファイルを処理途中で分けるため別々に宣言
        List<String> childrenDirPathList = new ArrayList<>();
        List<String> childrenFilePathList = new ArrayList<>();

        // ----------------------------------
        // 通常のFileクラスの一覧を取得
        // ----------------------------------
        File[] childrenFilesAry = this.listFiles();

        // 例外処理, ファイルだと一覧がないのでnullを返す
        if (childrenFilesAry == null) {
            return null;
        }

        // ----------------------------------
        // このメソッド特有の変換処理
        // ----------------------------------
        //// 親ディレクトリのリストへの追加
        if ( this.getParentFile().list() != null ) {
            //親ディレクトリがあれば、if(this.getParentFile().exists())だとダメ
            childrenDirPathList.add(".." + System.getProperty("file.separator"));
        }

        //// 通常ディレクトリのリストへの追加
        for (File childFile : childrenFilesAry) {
            if ( childFile.isDirectory() ) {
                childrenDirPathList.add(childFile.getName() + System.getProperty("file.separator"));
            } else {
                childrenFilePathList.add(childFile.getName());
            }
        }

        // ----------------------------------
        // リストを合体させてreturn
        // ----------------------------------
        childrenDirPathList.addAll(childrenFilePathList);

        return childrenDirPathList;
    }

    /************************************
     * 再帰的に子ファイルのFileを取得する関数
     * @param filterExtensionAry 絞り込みの拡張子の文字列の配列, nullだと絞込を行わない
     * @return 取得したFileのリスト
     */
    @SuppressWarnings("WeakerAccess")
    public List<File> getAllFileList(String[] filterExtensionAry) {
        List<File> fileList = new ArrayList<>();

        // このディレクトリの指定拡張子のファイルと全ての子ディレクトリを取得
        File[] childrenFiles = this.listFiles(
                new MyFileFilter(filterExtensionAry, true)
        );

        for(File childFile: childrenFiles) {
            if ( childFile.isDirectory() ) {
                fileList.addAll(
                        new FileExtended(childFile.getPath()).getAllFileList(filterExtensionAry)
                );
            } else {
                fileList.add(childFile);
            }
        }

        return fileList;
    }

    /************************************
     * 再帰的に子ファイルの絶対パスを取得する関数
     * @param filterExtensionAry 絞り込みの拡張子の文字列の配列, nullだと絞込を行わない
     * @return 取得したファイルの絶対パスのリスト
     */
    public List<String> getAllFilePathList(String[] filterExtensionAry) {
        List<File> allFilesList = this.getAllFileList(filterExtensionAry);

        List<String> allFilePathList = new ArrayList<>();
        for(File file : allFilesList) {
            allFilePathList.add(file.getAbsolutePath());
        }
        return allFilePathList;
    }

    /************************************
     * File.listFiles() 用のクラス、絞込み用
     */
    public static class MyFileFilter implements FileFilter {
        private final String[] extensionAry;
        private final boolean dirOk;

        public MyFileFilter(String[] extensionAry, @SuppressWarnings("SameParameterValue") boolean dirOk) {
            if (extensionAry == null) {
                this.extensionAry = null;
            } else {
                this.extensionAry = extensionAry.clone();
            }
            this.dirOk = dirOk;
        }

        /************************************
         * ここでtruを返せば絞り込みに残る
         */
        @Override
        public boolean accept(File file) {
            // ----------------------------------
            // 例外処理
            // ----------------------------------
            if (this.extensionAry == null) {
                return true;
            }
            // ----------------------------------
            // 通常処理
            // ----------------------------------
            for (String extensionStr : this.extensionAry) {
                // ディレクトリOK設定の時でディレクトリの時trueを返す
                if ( file.isDirectory() && this.dirOk) {
                    return true;
                }

                // 拡張子があっている時trueを返す
                if (file.getName().toLowerCase(Locale.ENGLISH).endsWith(
                        "." + extensionStr.toLowerCase(Locale.ENGLISH)
                ) ) {
                    return true;
                }
            }
            return false;
        }
    }

//    /************************************
//     * このファイルが指定拡張子を持っているか
//     * @param extensionAry 拡張子の配列 {"jpg", "png"}, nullだと全てtrueが返る
//     * @return 指定拡張をを持っていたらtrue
//     */
//    public boolean hasExtension(String[] extensionAry) {
//        // ----------------------------------
//        // 例外処理
//        // ----------------------------------
//        if (extensionAry == null) {
//            return true;
//        }
//
//        // ----------------------------------
//        // 通常処理
//        // ----------------------------------
//        for (String extensionStr : extensionAry) {
//            if ( this.getName().toLowerCase(Locale.ENGLISH).endsWith(
//                    "." + extensionStr.toLowerCase(Locale.ENGLISH)
//            )) {
//                return true;
//            }
//        }
//        return false;
//    }



}