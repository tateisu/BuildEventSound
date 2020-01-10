# BuildEvent Sound
IntelliJ IDEA用のプラグインです。ビルド終了時にコマンドを実行します。
たとえばffplayを起動して何か音声ファイルを再生するなどできます。

### 機能制限
- IntelliJ IDEA でJavaやKotlinを直接扱う場合は BeforeCompile, BuildSuccess, BuildWarning, BuildError, TestPassed, TestDefect イベントを受け取れます。
- Android Studio でGradleを使う場合は BuildSuccess, BuildWarning, BuildError イベントを受け取れます。
- IntelliJ IDEA でGradleを使う場合はイベントを検出できません。

### 導入1
- インターネットからffmpegを適当にインストールして、同梱されているffplayの実行ファイルのパスをメモしておきます。
- このプラグインのリリースページ https://github.com/tateisu/BuildEventSound/releases のAssetsからプラグインのjarファイルと`comfig.txt.sample`ファイルをダウンロードしておきます。

### 音声ファイルと設定ファイルの用意
- 適当なフォルダに音声ファイルをいくつか用意します。
- `config.txt.sample`を同じフォルダに置いて、ファイル名を`config.txt`に名前を変更します。
- `config.txt` をエディタで適当に編集します。 設定項目についてはファイル中のコメントを確認してください。

### 導入2
- IDEのメニューのFile/Settings/Pluginsを開きます
- 右上の設定マークを押して「Install Plugin from Disk…」を開きます
- プラグインのjarファイルを指定します。
- IDEを再起動します。
- IDEのメニューのFile/Settings/Other Settings/BuildEvent Soundを開きます。
- Config file path の項目に、先ほど編集したconfig.txtのファイルパスをフルパスで指定します。
- Applyボタンを押すと設定が読み込まれて、ビルド完了時に音がなります。

### トラブルシューティング
IDEのメニューのHelp/Show Log in Explorerを選ぶとidea.logのあるフォルダが開かれるので、適当に読んで調べてください。

### 最後に
[巻乃もなかはいいぞ](https://twitter.com/monaka_0_0_7)
