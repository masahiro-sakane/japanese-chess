# 将棋アプリ - Japanese Chess

CQRSイベントソーシングを用いた将棋アプリケーション

## 技術スタック

### バックエンド
- **Java 17+**
- **Spring Boot 3.2.1**
- **EventStoreDB 23.10** - イベントストア
- **PostgreSQL 16** - Read Model用データベース

### フロントエンド
- **React 18.2**
- **TypeScript 5.3**
- **Vite 5.0** - ビルドツール
- **Zustand 4.4** - 状態管理

### 機能
- 対局機能(人 vs 人)
- 棋譜の記録・再生
- AI対戦

## アーキテクチャ

このアプリケーションはCQRS(Command Query Responsibility Segregation)とイベントソーシングパターンを採用しています。

### コマンド側(Write Model)
```
[Frontend] → [REST API] → [Command Handler] → [Aggregate] → [EventStoreDB]
```

### クエリ側(Read Model)
```
[EventStoreDB] → [Projection] → [PostgreSQL] → [Query API] → [Frontend]
```

## 環境構築

### 前提条件
- Java 17以上
- Node.js 18以上
- Docker & Docker Compose
- Gradle(または内蔵のGradle Wrapper使用)

### 1. リポジトリのクローン
```bash
git clone <repository-url>
cd japanese-chess
```

### 2. インフラストラクチャの起動
```bash
docker-compose up -d
```

以下のサービスが起動します:
- **EventStoreDB**: http://localhost:2113 (管理画面)
- **PostgreSQL**: localhost:5432

### 3. バックエンドの起動

```bash
cd backend
./gradlew bootRun
```

または、IDEでJapaneseChessApplication.javaを実行

アプリケーションは http://localhost:8080 で起動します

### 4. フロントエンドの起動

```bash
cd frontend
npm install
npm run dev
```

アプリケーションは http://localhost:5173 で起動します

## テスト実行

### バックエンドテスト
```bash
cd backend
./gradlew test
```

### フロントエンドテスト
```bash
cd frontend
npm test
```

### E2Eテスト
```bash
cd frontend
npm run test:e2e
```

## 開発ガイド

### プロジェクト構造

```
japanese-chess/
├── backend/                 # Spring Bootバックエンド
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/japanesechess/
│   │   │   │   ├── config/          # 設定
│   │   │   │   ├── domain/          # ドメインモデル
│   │   │   │   ├── command/         # コマンド
│   │   │   │   ├── event/           # イベント
│   │   │   │   ├── aggregate/       # アグリゲート
│   │   │   │   ├── projection/      # プロジェクション
│   │   │   │   ├── query/           # クエリ
│   │   │   │   └── api/             # REST API
│   │   │   └── resources/
│   │   └── test/
│   └── build.gradle
├── frontend/                # Reactフロントエンド
│   ├── src/
│   │   ├── components/      # UIコンポーネント
│   │   ├── hooks/           # カスタムフック
│   │   ├── services/        # API呼び出し
│   │   ├── stores/          # 状態管理
│   │   └── types/           # TypeScript型定義
│   └── package.json
├── docs/                    # ドキュメント
└── docker-compose.yml       # インフラ定義
```

### EventStoreDB管理画面

http://localhost:2113 にアクセスすると、EventStoreDBの管理画面が表示されます。
- ストリーム一覧
- イベント詳細
- プロジェクション管理

### PostgreSQL接続情報

```
Host: localhost
Port: 5432
Database: japanese_chess
Username: chess_user
Password: chess_password
```

## 実装フェーズ

- [x] Phase 0: プロジェクト初期化 & インフラ構築
- [ ] Phase 1: ドメインモデル実装(コマンド側)
- [ ] Phase 2: イベントストア統合
- [ ] Phase 3: コマンドハンドラ & REST API
- [ ] Phase 4: クエリ側実装(Projection)
- [ ] Phase 5: フロントエンド基本UI
- [ ] Phase 6: 棋譜記録・再生機能
- [ ] Phase 7: AI対戦機能
- [ ] Phase 8: 本番環境対応

## ライセンス

MIT
