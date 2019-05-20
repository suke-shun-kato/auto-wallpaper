package xyz.goodistory.autowallpaper.wpchange;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WpUrisGetterInstagram_existsImgUrl_Test {
    private Method mMethod;

    @Before
    public void before() throws Exception {
        mMethod = WpUrisGetterInstagram.class.getDeclaredMethod(
                "existsImgUrl", List.class, String.class);
        mMethod.setAccessible(true);
    }

    private static Map<String, String> makeMap(String imgUri, String actionUri) {
        Map<String, String> uris = new HashMap<>();
        uris.put("img", imgUri);
        uris.put("action", actionUri);

        return uris;
    }

    @Test
    public void normalExist() throws Exception {
        List<Map<String, String>> urisList = new ArrayList<>();
        urisList.add(makeMap("aaa", "ccccccc"));
        urisList.add(makeMap("ccccccc", "ffffffff"));
        urisList.add(makeMap("bdfs", "ffffffff"));


        assertTrue( (boolean)mMethod.invoke(null, urisList, "aaa") );
        assertTrue( (boolean)mMethod.invoke(null, urisList, "ccccccc") );
        assertTrue( (boolean)mMethod.invoke(null, urisList, "bdfs"));

        assertFalse( (boolean)mMethod.invoke(null, urisList, "ffffffff") );
        assertFalse( (boolean)mMethod.invoke(null, urisList, "aafsdaffff") );
        assertFalse( (boolean)mMethod.invoke(null, urisList, "") );
    }

    @Test
    public void specialExist() throws Exception {
        List<Map<String, String>> urisList = new ArrayList<>();
        urisList.add(makeMap("aaa", "ccccccc"));
        urisList.add(makeMap("", "ffffffff"));
        urisList.add(makeMap("bdfss", "ffffffff"));


        assertTrue( (boolean)mMethod.invoke(null, urisList, "aaa") );
        assertTrue( (boolean)mMethod.invoke(null, urisList, "") );
        assertTrue( (boolean)mMethod.invoke(null, urisList, "bdfss"));

        assertFalse( (boolean)mMethod.invoke(null, urisList, "aaaa") );
        assertFalse( (boolean)mMethod.invoke(null, urisList, "aafsdaffff") );
    }

    @Test
    public void doubleExist() throws Exception {
        List<Map<String, String>> urisList = new ArrayList<>();
        urisList.add(makeMap("fff", "cccc"));
        urisList.add(makeMap("ccc", "cccc"));
        urisList.add(makeMap("fff", "ddd"));
        urisList.add(makeMap("aaa", "ccccccc"));
        urisList.add(makeMap("fff", "vvv"));
        urisList.add(makeMap("aaa", "ccccccc"));

        assertTrue( (boolean)mMethod.invoke(null, urisList, "fff") );
        assertTrue( (boolean)mMethod.invoke(null, urisList, "ccc") );
        assertTrue( (boolean)mMethod.invoke(null, urisList, "aaa") );

        assertFalse( (boolean)mMethod.invoke(null, urisList, "aa") );
        assertFalse( (boolean)mMethod.invoke(null, urisList, "aaaa") );
        assertFalse( (boolean)mMethod.invoke(null, urisList, "fsda") );
        assertFalse( (boolean)mMethod.invoke(null, urisList, "") );

    }
}
