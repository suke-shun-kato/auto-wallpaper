package xyz.goodistory.autowallpaper.wpchange;

import java.util.List;

public abstract class WpUrisGetter {
//    public abstract List<WpUris> getWpUrisList();
    /**
     * ここでContext を引数に入れてしまうと拡張性がなくなるのでしない
     * @return 画像のURL一覧
     * @throws Exception
     */
    public abstract List<ImgGetter> getImgGetterList() throws Exception;
}
