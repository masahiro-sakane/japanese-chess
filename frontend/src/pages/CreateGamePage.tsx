import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { token } from '@atlaskit/tokens'
import Button from '@atlaskit/button/new'
import { RadioGroup } from '@atlaskit/radio'
import TextField from '@atlaskit/textfield'
import Form, { Field, FormSection, HelperMessage } from '@atlaskit/form'
import SectionMessage from '@atlaskit/section-message'
import PageHeader from '@atlaskit/page-header'
import { gameService } from '../services/gameService'
import { AiDifficultySelector } from '../components/AiDifficultySelector'
import type { AiDifficulty } from '../types/api'

const GAME_MODE_OPTIONS = [
  { label: '対人戦 — 2人のプレイヤーで対戦', value: 'pvp', name: 'game-mode' },
  { label: 'AI対戦 — AIと対戦（あなたは先手）', value: 'ai', name: 'game-mode' },
]

export const CreateGamePage: React.FC = () => {
  const navigate = useNavigate()
  const [blackPlayerId, setBlackPlayerId] = useState<string>(() => crypto.randomUUID())
  const [whitePlayerId] = useState<string>(() => crypto.randomUUID())
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isAiGame, setIsAiGame] = useState(false)
  const [aiDifficulty, setAiDifficulty] = useState<AiDifficulty>('BEGINNER')

  const handleModeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setIsAiGame(e.currentTarget.value === 'ai')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)

    const effectiveWhitePlayerId = isAiGame ? 'AI_PLAYER' : whitePlayerId

    try {
      const response = await gameService.createGame({
        blackPlayerId,
        whitePlayerId: effectiveWhitePlayerId,
        aiGame: isAiGame,
        aiDifficulty: isAiGame ? aiDifficulty : undefined,
      })

      let retries = 0
      const maxRetries = 20
      let gameFound = false

      while (retries < maxRetries && !gameFound) {
        await new Promise(resolve => setTimeout(resolve, 300))
        try {
          const checkResponse = await fetch(`http://localhost:8080/api/queries/games/${response.gameId}`)
          if (checkResponse.ok) {
            gameFound = true
          }
        } catch {
          // game not found yet
        }
        retries++
      }

      if (!gameFound) {
        throw new Error('ゲームの作成に時間がかかっています。少し待ってから対局一覧から開いてください。')
      }

      navigate(`/game/${response.gameId}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : '対局の作成に失敗しました')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <PageHeader>新規対局の作成</PageHeader>

      {error && (
        <div style={{ marginBottom: token('space.200', '16px') }}>
          <SectionMessage appearance="error" title="エラーが発生しました">
            {error}
          </SectionMessage>
        </div>
      )}

      <div style={{
        backgroundColor: '#FFFFFF',
        border: `1px solid ${token('color.border', '#DFE1E6')}`,
        borderRadius: 8,
        padding: token('space.300', '24px'),
        marginBottom: token('space.200', '16px'),
      }}>
        <Form onSubmit={handleSubmit}>
          {({ formProps }) => (
            <form {...formProps}>
              {/* 対局モード */}
              <FormSection title="対局モード">
                <Field name="game-mode" label="">
                  {() => (
                    <RadioGroup
                      options={GAME_MODE_OPTIONS}
                      value={isAiGame ? 'ai' : 'pvp'}
                      onChange={handleModeChange}
                      isDisabled={isLoading}
                    />
                  )}
                </Field>
              </FormSection>

              {/* AI難易度 */}
              {isAiGame && (
                <FormSection title="AI設定">
                  <Field name="ai-difficulty" label="">
                    {() => (
                      <AiDifficultySelector
                        difficulty={aiDifficulty}
                        onChange={setAiDifficulty}
                        disabled={isLoading}
                      />
                    )}
                  </Field>
                </FormSection>
              )}

              {/* プレイヤーID */}
              <FormSection title="プレイヤー情報">
                <Field name="blackPlayerId" label="先手（あなた）プレイヤーID" isRequired>
                  {({ fieldProps }) => (
                    <>
                      <div style={{ display: 'flex', gap: token('space.100', '8px'), alignItems: 'center' }}>
                        <div style={{ flex: 1 }}>
                          <TextField
                            {...fieldProps}
                            value={blackPlayerId}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setBlackPlayerId(e.target.value)}
                            isDisabled={isLoading}
                            elemAfterInput={undefined}
                          />
                        </div>
                        <Button
                          appearance="subtle"
                          onClick={() => setBlackPlayerId(crypto.randomUUID())}
                          isDisabled={isLoading}
                        >
                          ランダム生成
                        </Button>
                      </div>
                      <HelperMessage>対局を識別するためのプレイヤーIDです</HelperMessage>
                    </>
                  )}
                </Field>

                {!isAiGame && (
                  <Field name="whitePlayerId" label="後手（相手）プレイヤーID">
                    {({ fieldProps }) => (
                      <>
                        <TextField
                          {...fieldProps}
                          value={whitePlayerId}
                          isReadOnly
                        />
                        <HelperMessage>後手のプレイヤーIDは自動生成されます</HelperMessage>
                      </>
                    )}
                  </Field>
                )}
              </FormSection>

              {/* アクション */}
              <div style={{ display: 'flex', gap: token('space.100', '8px'), marginTop: token('space.300', '24px') }}>
                <Button
                  type="submit"
                  appearance="primary"
                  isLoading={isLoading}
                  isDisabled={isLoading}
                >
                  {isAiGame ? 'AIと対局を開始' : '対局を開始'}
                </Button>
                <Button
                  appearance="subtle"
                  isDisabled={isLoading}
                  onClick={() => navigate('/')}
                >
                  キャンセル
                </Button>
              </div>
            </form>
          )}
        </Form>
      </div>

      {/* ヒント */}
      <SectionMessage appearance="information" title="ヒント">
        <ul style={{ margin: 0, paddingLeft: token('space.200', '16px') }}>
          <li>AI対戦モードでは、あなたが先手（黒）でAIが後手（白）になります</li>
          <li>難易度が高いほどAIは強くなりますが、応答に時間がかかります</li>
          <li>対人戦では2ブラウザタブやウィンドウで対戦できます</li>
        </ul>
      </SectionMessage>
    </div>
  )
}
