package xyz.monogatari.suke.autowallpaper;

import java.util.HashMap;
import java.util.Map;

/**
 * 変数の連想配列
 * Created by k-shunsuke on 2018/02/10.
 */

@SuppressWarnings("WeakerAccess")
public class ImgSourceAsso {
    static public Map get(String key) {
        Map<String, Map<String, Object>> topMap = new HashMap<>();


        Map<String, Object> item1Map = new HashMap<>();
        item1Map.put("icon", R.drawable.ic_dir);
        topMap.put("ImgGetterDir", item1Map);


        Map<String, Object> item2Map = new HashMap<>();
        item2Map.put("icon", R.drawable.ic_twitter);
        topMap.put("ImgGetterTw", item2Map);

        return topMap.get(key);
    }
}

