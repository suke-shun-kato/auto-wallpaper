現在、一時的にソースコードを公開中です

# アピールポイント

- カスタムプリファレンスを自分で何個か作っています

- その他、Activity, Fragment, Service, BroadcastReceiver, 非同期処理, コンテンツプロバイダ, Media Store, Notification, RecyclerView,  Preference, Android X など使って書いてます

# 補足ポイント

- 最初の方に書いたコードはコーディング規則があやふやです。現在はグーグルのAPIの書き方に合わせていて、徐々に修正中です。

- 最初の方に書いたコードは、色々未熟な部分があったりします。変数の宣言に `final` を使ってなかったり、ServiceからActivityに通信するときbindじゃなくてBroadcastReceiver使ってたりとか、RecyclerViewじゃなくてListViewを使ってたりなど

- その他色々絶賛リファクタリング中です

- 初期コミットが '17/12ですが、途中でリポジトリ名が気に入らなかったかからコミットし直したので、4ヶ月前ぐらいから作ってます。


# ここから下は通常のREADMEです！

# G.I.S 自動壁紙チェンジャー

このアプリは壁紙をスマホ内やSNSから取得してランダムにセットするアプリです。

また、壁紙変更は設定時間ごとや電源OFF時のタイミングで変更するように設定可能です。

詳しくは[Google Play](https://play.google.com/store/apps/details?id=xyz.goodistory.autowallpaper)で公開中です。

## 公開場所

[Google Play - G.I.S 自動壁紙チェンジャー](https://play.google.com/store/apps/details?id=xyz.goodistory.autowallpaper)

# 言語

- Android Java
- Kotlin

# 作者

## メインプログラム

- 加藤俊祐

## アイコン制作

- [etsu-design様](https://www.etsu-design.net/)

## 使用ライブラリ

- [Universal image loader](https://github.com/nostra13/Android-Universal-Image-Loader)
- [Scribe Java](https://github.com/scribejava/scribejava)


# 参考リンク

- [Qiita - 【今日からできる】コミットメッセージに 「プレフィックス」 をつけるだけで、開発効率が上がった話](https://qiita.com/numanomanu/items/45dd285b286a1f7280ed)
- [Qiita - Androidアプリ制作のためのリンク集（初心者用）](https://qiita.com/suke/items/47475fff4bd62750d922)

- [Bitbucket - program_note](https://bitbucket.org/suke-shun-kato/program_note/src)
