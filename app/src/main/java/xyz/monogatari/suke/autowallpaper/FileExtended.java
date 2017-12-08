package xyz.monogatari.suke.autowallpaper;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
     * @return ディレクトリ＆ファイル一覧
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

        // 例外処理
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
}