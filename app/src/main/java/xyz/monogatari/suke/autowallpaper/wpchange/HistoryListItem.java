package xyz.monogatari.suke.autowallpaper.wpchange;

/**
 * historiesテーブルを取得時に格納しておくデータオブジェクト、List作るよう
 * Created by k-shunsuke on 2018/02/07.
 */

public class HistoryListItem {
    private long id = 0;
    private long source_kind;
    private String img_uri;
    private String intent_action_uri;
    private String created_at_local;

    public HistoryListItem(long id, long source_kind, String img_uri, String intent_action_uri, String created_at_local) {
        this.id = id;
        this.source_kind = source_kind;
        this.img_uri = img_uri;
        this.intent_action_uri = intent_action_uri;
        this.created_at_local = created_at_local;
    }

    public long getId() {
        return id;
    }

    public long getSource_kind() {
        return source_kind;
    }

    public String getImg_uri() {
        return img_uri;
    }

    public String getIntent_action_uri() {
        return intent_action_uri;
    }

    public String getCreated_at_local() {
        return created_at_local;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSource_kind(long source_kind) {
        this.source_kind = source_kind;
    }

    public void setImg_uri(String img_uri) {
        this.img_uri = img_uri;
    }

    public void setIntent_action_uri(String intent_action_uri) {
        this.intent_action_uri = intent_action_uri;
    }

    public void setCreated_at_local(String created_at_local) {
        this.created_at_local = created_at_local;
    }
}
