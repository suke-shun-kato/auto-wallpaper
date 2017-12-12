package xyz.monogatari.suke.autowallpaper.util;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created by k-shunsuke on 2017/12/12.
 */
@SuppressWarnings("ALL")
public class FileExtended_getAllFilePathList_Test {
    private static final String SP = System.getProperty("file.separator");

    @Rule
    public final TemporaryFolder tf = new TemporaryFolder();

    @After
    public void after() throws Exception {
        tf.delete();
    }

    @Test
    public void flat() throws Exception {
        // ----------------------------------
        // テスト用ディレクトリを作成
        // ----------------------------------
        tf.newFile("aaaaa.jpg");
        tf.newFile("bbbbb.png");
        tf.newFile("ccccc.txt");
        tf.newFile("ddddd.xls");
        tf.newFile("eeeee.png.txt");
//        tf.newFolder("aaaaaa");
//        tf.newFile("aaaaaa"+SP+"ccccc.png");

        // ----------------------------------
        // 準備、加工
        // ----------------------------------
        FileExtended testFileEx = new FileExtended(tf.getRoot().getPath());

        String[] testExtensions = {"jpg", "png", "gif"};
        List<String> allFilesPathList = testFileEx.getAllFilePathList(testExtensions);



        // ----------------------------------
        //
        // ----------------------------------
        assertThat(allFilesPathList, hasItems(
                tf.getRoot().getAbsolutePath() + SP+ "aaaaa.jpg"
                ,tf.getRoot().getAbsolutePath()+ SP + "bbbbb.png"
        ));

        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+ "ccccc.txt"
        )));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+ "ddddd.xls"
        )));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+ "eeee.ffff"
        )));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+ "eeeee.png.txt"
        )));
    }

    /************************************
     * 多段のテスト
     */
    @Test
    public void deep() throws Exception {
        // ----------------------------------
        // テスト用ディレクトリを作成
        // ----------------------------------
        tf.newFile("aaaaa.java");
        tf.newFile("bbbbb.png");

        tf.newFolder("aaa");
        tf.newFile("aaa"+SP+"ccccc.js");
        tf.newFile("aaa"+SP+"ddddd.jss");

        tf.newFolder("bbb","ccc");  // bbb/ccc のディレクトリを作成
        tf.newFile("bbb"+SP+"ccc"+SP+"ddddd.php");
        tf.newFile("bbb"+SP+"ccc"+SP+"eeeee.jss");

        tf.newFolder("bbb","ddd","eee","fff");
        tf.newFile("bbb"+SP+"ddd"+SP+"eee"+SP+"fff"+SP+"fffff.sql");
        tf.newFile("bbb"+SP+"ddd"+SP+"eee"+SP+"fff"+SP+"ggggg.sql.fdd");

        // ----------------------------------
        // 準備、加工
        // ----------------------------------
        FileExtended testFileEx = new FileExtended(tf.getRoot().getPath());

        String[] testExtensions = {"java", "php", "js", "sql"};
        List<String> allFilesPathList = testFileEx.getAllFilePathList(testExtensions);
        
        // ----------------------------------
        //
        // ----------------------------------
        assertThat(allFilesPathList, hasItems(
                tf.getRoot().getAbsolutePath() + SP+"aaaaa.java"
                ,tf.getRoot().getAbsolutePath()+ SP+"aaa"+SP+"ccccc.js"
                ,tf.getRoot().getAbsolutePath()+ SP+"bbb"+SP+"ccc"+SP+"ddddd.php"
                ,tf.getRoot().getAbsolutePath()+ SP+"bbb"+SP+"ddd"+SP+"eee"+SP+"fff"+SP+"fffff.sql"
        ));

        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+ "bbbbb.png"
        )));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+ "fdsafds.php"
        )));
        // ディレクトリ
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+ "aaa"
        )));


        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+"aaa"+SP+"ddddd.jss"
        )));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+"bbb"+SP+"ccc"+SP+"eeeee.jss"
        )));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+"bbb"+SP+"ddd"+SP+"eee"+SP+"fff"+SP+"ggggg.sql.fdd"
        )));

        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+"jlfsada"+SP+"ggggg.sql.fdd"
        )));

    }
    /************************************
     * 拡張子が{}のテスト
     */
    @Test
    public void extensionEmpty() throws Exception {
        // ----------------------------------
        // テスト用ディレクトリを作成
        // ----------------------------------
        tf.newFile("aaaaa.java");

        tf.newFolder("aaa", "bbb");
        tf.newFile("aaa" + SP + "bbb" + SP + "bbbbb.js");

        // ----------------------------------
        // 準備、加工
        // ----------------------------------
        FileExtended testFileEx = new FileExtended(tf.getRoot().getPath());

        String[] testExtensions = {};
        List<String> allFilesPathList = testFileEx.getAllFilePathList(testExtensions);

        // ----------------------------------
        //
        // ----------------------------------
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath() + SP+"aaaaa.java"
        )));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+"aaa" + SP + "bbb"+ SP +"bbbbb.js"
        )));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+"aaa" + SP + "ccc"+ SP +"ccccc.js"
        )));
    }

    /************************************
     * 拡張子がnullのテスト
     */
    @Test
    public void extensionNull() throws Exception {
        // ----------------------------------
        // テスト用ディレクトリを作成
        // ----------------------------------
        tf.newFile("aaaaa.java");

        tf.newFolder("aaa", "bbb");
        tf.newFile("aaa" + SP + "bbb"+ SP +"bbbbb.js");

        // ----------------------------------
        // 準備、加工
        // ----------------------------------
        FileExtended testFileEx = new FileExtended(tf.getRoot().getPath());

        String[] testExtensions = null;
        List<String> allFilesPathList = testFileEx.getAllFilePathList(testExtensions);
        
        // ----------------------------------
        //
        // ----------------------------------
        assertThat(allFilesPathList, hasItems(
                tf.getRoot().getAbsolutePath() + SP+"aaaaa.java"
                ,tf.getRoot().getAbsolutePath()+ SP+"aaa" + SP + "bbb"+ SP +"bbbbb.js"
        ));
        assertThat(allFilesPathList, not(hasItems(
                tf.getRoot().getAbsolutePath()+ SP+"jlfsada"+SP+"ggggg.sql.fdd"
        )));


    }
}