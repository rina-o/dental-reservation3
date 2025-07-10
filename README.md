歯科予約システム - dental_reservation3

 Java 21 + Spring Boot 3.5.3 + MySQL + Thymeleaf で開発した、歯科医院向けの予約管理Webアプリケーションです。  
 新患・再診の予約から、確認・変更・キャンセルまで、患者がスマホやPCから操作可能です。



 概要

 対象ユーザー	：歯科医院の患者（新患・再診）  
 対応画面　   	：新患予約/患者情報仮登録・再診ログイン・日時選択・確認画面・予約完了・予約確認・変更・キャンセル



使用技術

バックエンド 		： Java 21  / Spring Boot 3.5.3   
フロントエンド	：Thymeleaf / HTML / CSS（最低限）   
データベース 		：MySQL   
開発ツール 		：IntelliJ IDEA / DBeaver / Git / GitHub   
その他 			：Spring Security（簡易ログイン）、JavaMailSender（予約完了メール送信） 、Lombok、Spring Session


開発支援・活用ツール

　このプロジェクトは以下のツールを活用しながら開発しました。  
	ChatGPT（GPT-4）						：全体構成や仕様確認に活用  
	Cursor（AIペアプログラミングエディタ）	：バリデーション・デバッグ・セキュリティなど。（時間短縮を目的とする）  
	


主な機能

・新患予約  
　治療内容・希望日時を選択→名前・メールアドレス・電話番号を入力して予約  

・再診患者予約  
　メールアドレス＋誕生日8桁で認証 → 予約日時の選択が可能  

・予約確認・変更・キャンセル  
　再診患者はログイン後、予約内容を一覧表示し、削除・新しい予約が可能  

・予約完了・キャンセル時の自動メール送信  
　SpringのJavaMailSenderで完了通知を送信  

・空き枠管理（ロジック）
  - 30分単位、1枠最大3件まで
  - 過去日時・満員枠は選択不可
  - 木曜・日曜・祝日は休診扱い（予約不可）



工夫ポイント

画面遷移			：新患は、希望日時を選択後に、仮患者登録へ  
セキュリティ		：再診ログインは、「メール or 患者番号」＋「誕生日8桁」の簡易認証  
DB制御ロジック	：予約枠の上限はSQLではなくサービス層で制御   
セッション管理	：再診ログインは30分で自動ログアウト＋予約完了時にセッションを明示的に終了（ログアウト）  
バリデーション	：DTO + `@Valid` による新患登録入力チェックを実装  



（画面イメージ）  

（※後日スクリーンショット・動画へのリンクを貼る予定）  



プロジェクト構成（抜粋）  

src/  
├── java/com/example/dental_reservation3/  
│   ├── controller  
│   ├── service  
│   ├── entity  
│   └── repository  
├── resources/  
│   ├── templates/  
│   └── application.properties  



画面構成（テンプレート一覧）

- `top.html`						：トップページ
- `identify.html`					：新患・再診の選択画面
- `new/input-info.html`			：新患情報入力（バリデーションあり）
- `new/confirm-reservation.html`	：新患の予約確認
- `login.html`						：再診ログイン（メール or 患者番号＋誕生日）
- `select-date-time.html`			：予約日時の選択（共通）
- `confirm-reservation.html`		：再診予約の確認
- `reservation-complete.html`		：予約完了画面
- `check-reservation.html`			：予約確認・変更・キャンセル画面
- `change-select-date-time.html`	：変更時の日時選択
- `change-reservation-confirm.html`：変更内容の確認
- `cancel-complete.html`			：キャンセル完了
- `error.html`						：エラー表示（ログイン失敗など）



制作背景・目的

　IT系職種への転職を目指す中で、実用的なJava×Spring Bootのポートフォリオとして作成 。  
　元歯科衛生士の実体験を活かし、「現場で本当に必要な予約機能」を意識して設計中。  


 作者

　Rina O.   
　GitHub: [@rina-o](https://github.com/rina-o)  


今後の改善予定

　学習・就職活動のタイミング上、現在は必要最低限の機能を中心に公開しています。   
今後、以下の機能を段階的に追加・改善していく予定です。    


改良したい点  
- 管理者側の予約管理画面  
- CSS・JavaScriptによるUI/UX向上  
- カレンダー表示の改善（UIの向上）  
- 患者や歯科医院スタッフの視点に立った、さらなる利便性の向上  


ご意見・ご提案がございましたら、お気軽にお寄せください。  