package xyz.monogatari.autowallpaper;

/**
 * historiesテーブルを取得時に格納しておくデータオブジェクト、List作るよう
 * Created by k-shunsuke on 2018/02/07.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class HistoryItemListDataStore {
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    private long id;
    private String source_kind;
    private String img_uri;
    private String intent_action_uri;
    private long created_at_unix;

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public HistoryItemListDataStore(long id, String source_kind, String img_uri, String intent_action_uri, long created_at_unix) {
        this.id = id;
        this.source_kind = source_kind;
        this.img_uri = img_uri;
        this.intent_action_uri = intent_action_uri;
        this.created_at_unix = created_at_unix;
    }
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public long getId() {
        return id;
    }

    public String getSource_kind() {
        return source_kind;
    }

    public String getImg_uri() {
        return img_uri;
    }

    public String getIntent_action_uri() {
        return intent_action_uri;
    }

    public long getCreated_at_unix() {
        return created_at_unix;
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    public void setId(long id) {
        this.id = id;
    }

    public void setSource_kind(String source_kind) {
        this.source_kind = source_kind;
    }

    public void setImg_uri(String img_uri) {
        this.img_uri = img_uri;
    }

    public void setIntent_action_uri(String intent_action_uri) {
        this.intent_action_uri = intent_action_uri;
    }

    public void setCreated_at_unix(long created_at_unix) {
        this.created_at_unix = created_at_unix;
    }
}
