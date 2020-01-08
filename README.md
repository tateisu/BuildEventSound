# BuildEventSound
Plugin for IntelliJ IDEA that runs command when build event happen.

IntelliJ IDEA用のプラグインです。ビルド終了時にコマンドを実行する能力があります。

### 導入1
ffmpegについてくるffplayコマンドを適当に探してインストールしておきます。

### 音声ファイルと設定ファイルの用意
- 適当なフォルダに音声ファイルをいくつか用意します。
- このリポジトリからconfig.txt.sampleをダウンロードして同じフォルダに置いて、config.txt にリネームします。
- config.txt をエディタで適当に編集します。 設定項目についてはファイル中のコメントを確認してください。

### 導入2
- Githubのリリースページに添付したjarファイルをダウンロードします。
- Android Studio のメニューのFile/Settings/Pluginsを開きます
- 右上の設定マークを押して「Install Plugin from Disk…」を開きます
- ダウンロードしたjarファイルを指定します。
- Android Studio を再起動します。

### 導入3
- Android Studio のメニューのFile/Settings/Other Settings/BuildEvent Soundを開きます。
- Config file path の項目に、先ほど編集したconfig.txtのファイルパスをフルパスで指定します。
- Appleボタンを押すと設定が読み込まれて、ビルド完了時に音がなります。

### トラブルシューティング
Android Studio のメニューのHelp/Show Log in Explorerを選ぶとidea.logのあるフォルダが開かれるので、適当に読んで調べてください。
 
