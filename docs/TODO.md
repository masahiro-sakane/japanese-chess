# TODO - 将棋アプリケーション ロードマップ

最終更新: 2026-02-24（テストカバレッジ強化完了）

## 凡例
- `[ ]` 未着手
- `[~]` 進行中
- `[x]` 完了

---

## 完了済み

- [x] **Issue #1** - 持ち駒を打つ機能（二歩・打ち歩詰め・行き所のない駒）
- [x] **Issue #2** - 投了機能
- [x] **Issue #3** - 王手検出
- [x] **Issue #4** - 詰み判定（自動終了）
- [x] **Issue #5** - 駒の成り改善（自動成り・選択成り・成りゾーン表示）
- [x] **Issue #6** - WebSocket リアルタイム更新（Spring STOMP + @stomp/stompjs）
- [x] **Issue #7** - 統計ページ拡張（Recharts グラフ）
- [x] **Issue #9** - AI対戦機能（Minimax + Alpha-Beta剪定、PR #28）
- [x] **テストカバレッジ強化** - フロントエンド 28% → 94%（213 tests、目標 80%+ 達成）

---

## Phase 6 - AI対戦完了 & テスト強化 ✅ 完了

### AI対戦機能（Issue #9）
- [x] バックエンド: AiEngine, RandomStrategy, MinimaxStrategy
- [x] バックエンド: AiGameRegistry, AiMoveScheduler（@Async）
- [x] バックエンド: BoardEvaluator, MoveGenerator
- [x] バックエンド: DB マイグレーション V4（is_ai_game, ai_difficulty）
- [x] フロントエンド: AiDifficultySelector コンポーネント
- [x] フロントエンド: AiThinkingIndicator コンポーネント
- [x] フロントエンド: CreateGamePage に AI モード選択 UI
- [x] コミット & PR 作成（PR #28）

### フロントエンドテストカバレッジ強化（28% → 94%）
- [x] api.ts テスト（15 tests）
- [x] gameService.ts テスト（17 tests）
- [x] gameStore.ts テスト（17 tests）
- [x] Board コンポーネントテスト（16 tests）
- [x] GameCard コンポーネントテスト（13 tests）
- [x] GameList コンポーネントテスト（13 tests）
- [x] Hand コンポーネントテスト（16 tests）
- [x] Layout コンポーネントテスト（7 tests）
- [x] Pagination コンポーネントテスト（13 tests）
- [x] Statistics コンポーネントテスト（8 tests）
- [x] StatusFilter コンポーネントテスト（7 tests）
- [x] AiDifficultySelector テスト（8 tests）
- [x] AiThinkingIndicator テスト（5 tests）
- [x] GameDetailPage テスト（26 tests）- 投了・王手・AI表示・駒打ち
- [x] CreateGamePage テスト（16 tests）
- [x] PromotionFeature テスト（7 tests）
- [x] SimplePages テスト（4 tests）
- [x] vite.config.ts に coverage 閾値設定（80%）

---

## Phase 7 - 認証 & E2Eテスト

### Issue #8 - プレイヤー認証（未着手）
- [ ] バックエンド: Spring Security + JWT 設定
- [ ] バックエンド: User エンティティ & リポジトリ
- [ ] バックエンド: 認証 API エンドポイント
  - [ ] POST `/api/auth/register` - ユーザー登録
  - [ ] POST `/api/auth/login` - ログイン（JWT 発行）
  - [ ] POST `/api/auth/refresh` - トークンリフレッシュ
  - [ ] POST `/api/auth/logout` - ログアウト
- [ ] バックエンド: API エンドポイントへの認証ガード
- [ ] バックエンド: DB マイグレーション（users テーブル）
- [ ] フロントエンド: ログイン・登録ページ
- [ ] フロントエンド: JWT トークン管理（localStorage / httpOnly Cookie）
- [ ] フロントエンド: 認証状態の Zustand ストア
- [ ] フロントエンド: 未認証時のリダイレクト（Protected Route）
- [ ] フロントエンド: ヘッダーにユーザー情報・ログアウトボタン

### E2Eテスト（Playwright）
- [ ] Playwright 設定ファイルの整備（playwright.config.ts）
- [ ] テストシナリオ: ゲーム作成から初手まで
- [ ] テストシナリオ: 駒移動の一連フロー（歩・飛・角・金・銀・桂・香・王）
- [ ] テストシナリオ: 持ち駒を打つフロー
- [ ] テストシナリオ: 成りダイアログの操作
- [ ] テストシナリオ: 投了フロー
- [ ] テストシナリオ: 詰み検出と自動終了
- [ ] テストシナリオ: AI対戦モード（BEGINNER でゲーム完了まで）

---

## Phase 8 - ルール完全実装 & 棋譜機能

### 千日手・持将棋ルール
- [ ] バックエンド: 局面ハッシュ計算（Zobrist ハッシュ推奨）
- [ ] バックエンド: 同一局面の回数カウント
- [ ] バックエンド: 千日手判定（4回繰り返し → 引き分け）
- [ ] バックエンド: 連続王手の千日手（→ 連続王手側の反則負け）
- [ ] バックエンド: 持将棋判定（双方の大駒・小駒点数チェック）
- [ ] バックエンド: `GameEndedEvent` に `SENNICHITE` / `IMPASSE` EndReason 追加
- [ ] フロントエンド: 千日手・持将棋の結果表示

### 棋譜保存・再生機能
- [ ] バックエンド: `GameReplayService` の完成
- [ ] バックエンド: KIF 形式エクスポート API（`KifExportService`）
- [ ] バックエンド: KIF 形式インポート API
- [ ] バックエンド: CSA 形式エクスポート
- [ ] フロントエンド: `GameReplayPage` の手順再生 UI
  - [ ] 「最初へ」「前へ」「次へ」「最後へ」ボタン
  - [ ] 手数スライダー
  - [ ] 各手の棋譜記号表示（例: ７六歩、８四歩）
- [ ] フロントエンド: KIF ファイルダウンロードボタン

### モバイル対応
- [ ] レスポンシブデザイン: 将棋盤のスケーリング
- [ ] タッチ操作対応（タップで駒選択・移動）
- [ ] モバイルレイアウト: 持ち駒エリアの配置変更
- [ ] Viewport 設定の最適化

---

## Phase 9 - 対局品質向上

### 対局時計（時間制限）
- [ ] バックエンド: 持ち時間エンティティ（per game, per player）
- [ ] バックエンド: 手番ごとの経過時間記録
- [ ] バックエンド: 時間切れ判定 & 自動終局
- [ ] バックエンド: 秒読みモード（例: 60秒）
- [ ] フロントエンド: 対局時計コンポーネント（リアルタイム表示）
- [ ] フロントエンド: ゲーム作成時の時間設定 UI

### 待った機能（対人戦）
- [ ] バックエンド: 待ったリクエスト API
- [ ] バックエンド: 待った承認・拒否ハンドリング
- [ ] バックエンド: `UndoMoveEvent` の追加
- [ ] フロントエンド: 「待った」ボタン（対人戦のみ）
- [ ] フロントエンド: 待たれた側への承認ダイアログ

### マッチメイキング（対戦待ち）
- [ ] バックエンド: 待機中ゲームの管理（ロビー機能）
- [ ] バックエンド: POST `/api/commands/games/match` - ランダムマッチ
- [ ] フロントエンド: 対戦相手を探す UI
- [ ] フロントエンド: マッチング中の待機画面

---

## Phase 10 - AI強化 & パフォーマンス

### AIの強化
- [ ] 開局定跡データベースの組み込み
- [ ] 置換表（Transposition Table）の実装
- [ ] キラームーブヒューリスティック
- [ ] 反復深化（Iterative Deepening）
- [ ] 探索深度の動的調整（ADVANCED: 4-6手）
- [ ] 評価関数の改善（駒の位置ボーナス、玉の安全度）

### パフォーマンス最適化
- [ ] バックエンド: イベントスナップショット（1000イベント以上で自動生成）
- [ ] バックエンド: Redis キャッシュ導入（read model）
- [ ] バックエンド: DB インデックス最適化
- [ ] フロントエンド: React.memo / useMemo の適用
- [ ] フロントエンド: 仮想スクロール（対局一覧の長大化対策）

---

## Phase 11 - UX向上 & 追加機能

### サウンド & アニメーション
- [ ] 駒移動アニメーション（CSS transition）
- [ ] 駒を打つ音・移動音の SE
- [ ] 王手時の警告音
- [ ] ゲーム終了時のエフェクト

### チュートリアル・ヒント
- [ ] 初心者向け合法手ハイライト（オプション）
- [ ] 駒の動き説明ツールチップ
- [ ] 将棋ルール説明ページ

### 観戦モード
- [ ] バックエンド: 観戦用 WebSocket サブスクリプション
- [ ] フロントエンド: 観戦ボタン（対局一覧から）
- [ ] フロントエンド: 観戦中の操作無効化 UI

### 国際化（i18n）
- [ ] i18next 導入
- [ ] 英語翻訳ファイル作成
- [ ] 言語切り替え UI

---

## 技術的負債

- [ ] フロントエンド: `GameDetailPage.tsx` の `any` 型を適切な型に修正
- [ ] フロントエンド: `AiThinkingIndicator` の表示タイミング（AI思考中フラグ連動）
- [ ] バックエンド: `AiGameRegistry` をインメモリから永続化へ（再起動で消える問題）
  - 注: `AiGameRegistryInitializer` で起動時に DB から復元する実装済み
- [ ] バックエンド: WebSocket がブロードキャスト方式のため、全クライアントが全ゲームの更新を受信してしまう問題
  - 修正案: `/topic/game/{gameId}` へのサブスクリプション制限
- [ ] CI/CD: GitHub Actions でのテスト自動実行設定の確認・強化
