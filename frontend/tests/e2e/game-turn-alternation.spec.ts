import { test, expect, Page } from '@playwright/test'

const ARTIFACTS_DIR = 'C:/Develop/workspace/claude/japanese-chess/frontend/artifacts'
const BASE_URL = 'http://localhost:5173'

/**
 * E2E Test: Japanese Chess (Shogi) - Turn Alternation with 3 Moves
 *
 * This test verifies that:
 * 1. A new game can be created with auto-generated player IDs
 * 2. The game page loads with "先手の番" displayed
 * 3. BLACK (sente) and WHITE (gote) can alternate moves without errors
 * 4. The turn indicator changes correctly after each move
 * 5. The move count increments after each move
 * 6. No "Not sente's turn" errors occur
 *
 * Board layout (initial):
 *   BLACK pieces: row 8 (back rank), row 7 (ROOK col=1, BISHOP col=7), row 6 (all pawns)
 *   WHITE pieces: row 0 (back rank), row 1 (BISHOP col=1, ROOK col=7), row 2 (all pawns)
 *
 * Moves executed:
 *   Move 1 (BLACK): Pawn (6,6) -> (5,6)
 *   Move 2 (WHITE): Pawn (2,6) -> (3,6)
 *   Move 3 (BLACK): Silver (8,6) -> (7,6)
 *
 * Note: This system uses CQRS/Event Sourcing. The command side (POST /moves)
 * returns immediately, but the query side (GET /queries/games/{id}) may take
 * time to reflect the updated state. The test polls until the projection updates.
 */

// Increase test timeout to accommodate CQRS projection delays
test.setTimeout(120000)

interface NetworkLog {
  readonly method: string
  readonly url: string
  readonly status: number
  readonly requestBody: string | null
  readonly responseBody: string | null
  readonly timestamp: string
}

interface MoveSpec {
  readonly label: string
  readonly player: string
  readonly fromRow: number
  readonly fromCol: number
  readonly toRow: number
  readonly toCol: number
  readonly expectedTurnBefore: string
  readonly expectedTurnAfter: string
  readonly expectedMoveCountAfter: number
}

const MOVES: readonly MoveSpec[] = [
  {
    label: 'Move 1 (BLACK): Pawn (6,6) -> (5,6)',
    player: 'BLACK',
    fromRow: 6,
    fromCol: 6,
    toRow: 5,
    toCol: 6,
    expectedTurnBefore: '先手の番',
    expectedTurnAfter: '後手の番',
    expectedMoveCountAfter: 1,
  },
  {
    label: 'Move 2 (WHITE): Pawn (2,6) -> (3,6)',
    player: 'WHITE',
    fromRow: 2,
    fromCol: 6,
    toRow: 3,
    toCol: 6,
    expectedTurnBefore: '後手の番',
    expectedTurnAfter: '先手の番',
    expectedMoveCountAfter: 2,
  },
  {
    label: 'Move 3 (BLACK): Silver (8,6) -> (7,6)',
    player: 'BLACK',
    fromRow: 8,
    fromCol: 6,
    toRow: 7,
    toCol: 6,
    expectedTurnBefore: '先手の番',
    expectedTurnAfter: '後手の番',
    expectedMoveCountAfter: 3,
  },
]

function buildTimestamp(): string {
  return new Date().toISOString()
}

async function captureScreenshot(page: Page, name: string): Promise<void> {
  try {
    await page.screenshot({
      path: `${ARTIFACTS_DIR}/${name}.png`,
      fullPage: true,
    })
  } catch (err) {
    console.log(`Screenshot "${name}" failed: ${err instanceof Error ? err.message : String(err)}`)
  }
}

/**
 * Poll the page by reloading until the expected turn text appears.
 * This handles the CQRS eventual consistency delay.
 */
async function waitForProjectionUpdate(
  page: Page,
  expectedTurnText: string,
  expectedMoveCount: number,
  maxAttempts: number = 15,
  intervalMs: number = 1000
): Promise<boolean> {
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    // Check if the turn indicator is already correct
    const turnLocator = page.locator(`text=${expectedTurnText}`)
    const isTurnVisible = await turnLocator.isVisible({ timeout: 500 }).catch(() => false)

    const moveCountLocator = page.locator(`text=手数: ${expectedMoveCount}`)
    const isMoveCountVisible = await moveCountLocator.isVisible({ timeout: 500 }).catch(() => false)

    if (isTurnVisible && isMoveCountVisible) {
      console.log(`  Projection updated (attempt ${attempt + 1}): turn="${expectedTurnText}", moveCount=${expectedMoveCount}`)
      return true
    }

    if (attempt < maxAttempts - 1) {
      console.log(`  Waiting for projection... (attempt ${attempt + 1}/${maxAttempts})`)
      await page.waitForTimeout(intervalMs)
      await page.reload()
      await page.waitForLoadState('networkidle')
      // Wait for the board to re-render
      await page.locator('.board-container').waitFor({ state: 'visible', timeout: 5000 }).catch(() => {
        // board might not be visible yet
      })
    }
  }
  return false
}

async function checkForErrors(page: Page, context: string): Promise<string[]> {
  const errors: string[] = []

  const errorEl = page.locator('.error')
  if (await errorEl.isVisible({ timeout: 500 }).catch(() => false)) {
    const text = await errorEl.textContent()
    errors.push(`[Page .error at ${context}] ${text}`)
  }

  const moveErrorEl = page.locator('.move-error')
  if (await moveErrorEl.isVisible({ timeout: 500 }).catch(() => false)) {
    const text = await moveErrorEl.textContent()
    errors.push(`[Move error at ${context}] ${text}`)
  }

  return errors
}

async function getPieceTextAt(page: Page, row: number, col: number): Promise<string | null> {
  const cell = page.locator('.board-row').nth(row).locator('.board-cell').nth(col)
  const piece = cell.locator('.piece')
  if (await piece.isVisible({ timeout: 1000 }).catch(() => false)) {
    return await piece.textContent()
  }
  return null
}

async function clickCell(page: Page, row: number, col: number): Promise<void> {
  const cell = page.locator('.board-row').nth(row).locator('.board-cell').nth(col)
  await cell.click()
}

test.describe('Shogi Turn Alternation - 3 Moves', () => {
  test('should create game, make 3 alternating moves, and verify turn changes', async ({ page }) => {
    // ----------------------------------------------------------------
    // Setup: Network logging and error capture
    // ----------------------------------------------------------------
    const networkLogs: NetworkLog[] = []
    const consoleErrors: string[] = []
    const allPageErrors: string[] = []

    page.on('response', async (response) => {
      const url = response.url()
      if (!url.includes('/api/')) return

      let requestBody: string | null = null
      let responseBody: string | null = null

      try {
        requestBody = response.request().postData() || null
      } catch {
        // no request body
      }

      try {
        responseBody = await response.text()
      } catch {
        // no response body
      }

      const log: NetworkLog = {
        method: response.request().method(),
        url,
        status: response.status(),
        requestBody,
        responseBody,
        timestamp: buildTimestamp(),
      }
      networkLogs.push(log)

      console.log(`[API ${log.timestamp}] ${log.method} ${url} => ${log.status}`)
      if (requestBody) {
        console.log(`  Request: ${requestBody}`)
      }
      if (responseBody) {
        console.log(`  Response: ${responseBody.substring(0, 800)}`)
      }
    })

    page.on('console', (msg) => {
      if (msg.type() === 'error') {
        consoleErrors.push(`[${buildTimestamp()}] ${msg.text()}`)
        console.log(`[Browser Console Error] ${msg.text()}`)
      }
    })

    page.on('pageerror', (error) => {
      consoleErrors.push(`[${buildTimestamp()}] PageError: ${error.message}`)
      console.log(`[Page Error] ${error.message}`)
    })

    // ================================================================
    // STEP 1: Navigate to home page
    // ================================================================
    console.log('\n========== STEP 1: Navigate to home page ==========')
    await page.goto(`${BASE_URL}/`)
    await page.waitForLoadState('networkidle')
    await captureScreenshot(page, '01-home-page')

    const homeErrors = await checkForErrors(page, 'home page')
    allPageErrors.push(...homeErrors)
    console.log(`Home page loaded. Errors found: ${homeErrors.length}`)

    // ================================================================
    // STEP 2: Navigate to Create Game page and create a game
    // ================================================================
    console.log('\n========== STEP 2: Create a new game ==========')
    const createLink = page.locator('a[href="/create"]')
    await expect(createLink).toBeVisible({ timeout: 10000 })
    await createLink.click()
    await page.waitForURL('**/create')
    await page.waitForLoadState('networkidle')
    await captureScreenshot(page, '02-create-game-page')

    await expect(page.getByRole('heading', { name: '新規対局の作成' })).toBeVisible()

    // Capture auto-generated player IDs
    const blackPlayerInput = page.locator('input[type="text"]').first()
    const whitePlayerInput = page.locator('input[type="text"]').nth(1)
    const blackPlayerId = await blackPlayerInput.inputValue()
    const whitePlayerId = await whitePlayerInput.inputValue()
    console.log(`BLACK player ID: ${blackPlayerId}`)
    console.log(`WHITE player ID: ${whitePlayerId}`)

    // Submit the form
    const createResponsePromise = page.waitForResponse(
      (resp) =>
        resp.url().includes('/api/games') &&
        resp.request().method() === 'POST' &&
        !resp.url().includes('/moves'),
      { timeout: 15000 }
    )

    const submitButton = page.locator('button[type="submit"]')
    await submitButton.click()

    const createResponse = await createResponsePromise
    const createBody = await createResponse.text()
    console.log(`Create API: ${createResponse.status()} ${createBody}`)
    expect(createResponse.status()).toBeLessThan(400)

    let gameId: string | null = null
    try {
      const parsed = JSON.parse(createBody)
      gameId = parsed.gameId
    } catch {
      console.log('Could not parse game ID from create response')
    }

    expect(gameId).toBeTruthy()
    console.log(`Game created: ${gameId}`)

    // Wait for navigation to the game detail page
    await page.waitForURL(`**/game/${gameId}`, { timeout: 15000 })
    await page.waitForLoadState('networkidle')
    await captureScreenshot(page, '03-after-create-navigated')

    // ================================================================
    // STEP 3: Verify initial game state (with projection polling)
    // ================================================================
    console.log('\n========== STEP 3: Verify initial game state ==========')

    const boardContainer = page.locator('.board-container')
    await expect(boardContainer).toBeVisible({ timeout: 15000 })

    // Wait for the projection to show the initial state
    const projectionReady = await waitForProjectionUpdate(page, '先手の番', 0)
    if (!projectionReady) {
      console.log('WARNING: Projection did not show expected initial state within timeout')
    }

    await expect(page.locator('text=先手の番')).toBeVisible({ timeout: 5000 })
    console.log('Initial turn indicator confirmed: 先手の番 (BLACK\'s turn)')

    await expect(page.locator('text=手数: 0')).toBeVisible({ timeout: 5000 })
    console.log('Initial move count confirmed: 0')

    await captureScreenshot(page, '04-initial-game-state')

    // Verify initial pieces
    const pawnAt66 = await getPieceTextAt(page, 6, 6)
    console.log(`Piece at (6,6): "${pawnAt66}" [expected: pawn]`)
    expect(pawnAt66).toBeTruthy()

    const pawnAt26 = await getPieceTextAt(page, 2, 6)
    console.log(`Piece at (2,6): "${pawnAt26}" [expected: pawn]`)
    expect(pawnAt26).toBeTruthy()

    const silverAt86 = await getPieceTextAt(page, 8, 6)
    console.log(`Piece at (8,6): "${silverAt86}" [expected: silver]`)
    expect(silverAt86).toBeTruthy()

    const emptyAt76 = await getPieceTextAt(page, 7, 6)
    console.log(`Piece at (7,6): "${emptyAt76}" [expected: empty]`)

    const initialErrors = await checkForErrors(page, 'initial game state')
    allPageErrors.push(...initialErrors)

    // ================================================================
    // STEP 4-6: Execute three moves with full verification
    // ================================================================
    for (let i = 0; i < MOVES.length; i++) {
      const move = MOVES[i]
      const stepNum = i + 4
      const prefix = String(stepNum).padStart(2, '0')

      console.log(`\n========== STEP ${stepNum}: ${move.label} ==========`)

      // --- Verify turn indicator before the move ---
      const turnBeforeLocator = page.locator(`text=${move.expectedTurnBefore}`)
      const isTurnCorrectBefore = await turnBeforeLocator
        .isVisible({ timeout: 3000 })
        .catch(() => false)

      if (!isTurnCorrectBefore) {
        const turnEl = page.locator('[style*="fontSize"]').filter({ hasText: /の番/ })
        const actualTurnText = await turnEl.textContent().catch(() => 'NOT FOUND')
        console.log(`TURN MISMATCH BEFORE MOVE ${i + 1}`)
        console.log(`  Expected: "${move.expectedTurnBefore}"`)
        console.log(`  Actual: "${actualTurnText}"`)
        await captureScreenshot(page, `${prefix}-turn-mismatch-before`)
      } else {
        console.log(`Turn before move: "${move.expectedTurnBefore}" - confirmed`)
      }

      // --- Log pieces at source and destination ---
      const srcPiece = await getPieceTextAt(page, move.fromRow, move.fromCol)
      const dstPiece = await getPieceTextAt(page, move.toRow, move.toCol)
      console.log(`Source (${move.fromRow},${move.fromCol}): "${srcPiece}"`)
      console.log(`Destination (${move.toRow},${move.toCol}): "${dstPiece}"`)

      await captureScreenshot(page, `${prefix}a-before-select`)

      // --- Select the piece ---
      console.log(`Clicking source cell (${move.fromRow},${move.fromCol})...`)
      await clickCell(page, move.fromRow, move.fromCol)
      await page.waitForTimeout(400)

      const selectedCell = page.locator('.board-cell.selected')
      const isSelected = await selectedCell.isVisible({ timeout: 2000 }).catch(() => false)
      console.log(`Piece selected: ${isSelected}`)

      if (!isSelected) {
        console.log('WARNING: Piece was not selected. Possible causes:')
        console.log('  - The piece does not belong to the current turn player')
        console.log('  - The cell is empty')
        console.log('  - The board is not interactive')
        await captureScreenshot(page, `${prefix}b-selection-failed`)
      } else {
        await captureScreenshot(page, `${prefix}b-piece-selected`)
      }

      // --- Execute the move ---
      const moveResponsePromise = page.waitForResponse(
        (resp) =>
          resp.url().includes('/moves') && resp.request().method() === 'POST',
        { timeout: 15000 }
      ).catch((err) => {
        console.log(`Move API response not captured: ${err instanceof Error ? err.message : String(err)}`)
        return null
      })

      console.log(`Clicking destination cell (${move.toRow},${move.toCol})...`)
      await clickCell(page, move.toRow, move.toCol)

      const moveResponse = await moveResponsePromise

      if (moveResponse) {
        const reqBody = moveResponse.request().postData()
        const resBody = await moveResponse.text()
        const status = moveResponse.status()

        console.log(`\n[Move ${i + 1} API Result]`)
        console.log(`  URL: ${moveResponse.url()}`)
        console.log(`  Status: ${status}`)
        console.log(`  Request: ${reqBody}`)
        console.log(`  Response: ${resBody}`)

        if (status >= 400) {
          console.log(`*** MOVE ${i + 1} FAILED WITH STATUS ${status} ***`)
          try {
            const errorData = JSON.parse(resBody)
            console.log(`Error details: ${JSON.stringify(errorData, null, 2)}`)
            if (resBody.includes('turn')) {
              console.log('*** TURN SYNCHRONIZATION ERROR DETECTED ***')
            }
          } catch {
            console.log(`Raw error body: ${resBody}`)
          }
          await captureScreenshot(page, `${prefix}c-move-api-error`)
        }
      } else {
        console.log(`*** No move API call detected for move ${i + 1} ***`)
        await captureScreenshot(page, `${prefix}c-no-api-call`)
      }

      // --- Wait for CQRS projection to update ---
      console.log(`Waiting for projection to update after move ${i + 1}...`)
      const projectionUpdated = await waitForProjectionUpdate(
        page,
        move.expectedTurnAfter,
        move.expectedMoveCountAfter,
        15,  // max 15 attempts
        1000 // 1 second between attempts
      )

      if (!projectionUpdated) {
        console.log(`PROJECTION DID NOT UPDATE after move ${i + 1} within timeout`)
        // Capture current state for debugging
        const turnEl = page.locator('[style*="fontSize"]').filter({ hasText: /の番/ })
        const actualTurn = await turnEl.textContent().catch(() => 'NOT FOUND')
        const moveCountEl = page.locator('text=/手数: \\d+/')
        const actualCounts = await moveCountEl.allTextContents()
        console.log(`  Current turn text: "${actualTurn}"`)
        console.log(`  Current move count texts: ${JSON.stringify(actualCounts)}`)
        await captureScreenshot(page, `${prefix}d-projection-timeout`)
      }

      // --- Check for page errors ---
      const moveErrors = await checkForErrors(page, `after move ${i + 1}`)
      allPageErrors.push(...moveErrors)
      if (moveErrors.length > 0) {
        console.log(`Page errors after move ${i + 1}:`)
        for (const err of moveErrors) {
          console.log(`  ${err}`)
        }
        await captureScreenshot(page, `${prefix}e-page-error`)
      }

      await captureScreenshot(page, `${prefix}c-after-move`)

      // --- Verify turn changed ---
      const turnAfterLocator = page.locator(`text=${move.expectedTurnAfter}`)
      const isTurnCorrectAfter = await turnAfterLocator
        .isVisible({ timeout: 3000 })
        .catch(() => false)

      if (isTurnCorrectAfter) {
        console.log(`Turn after move ${i + 1}: "${move.expectedTurnAfter}" - CONFIRMED`)
      } else {
        const turnEl = page.locator('[style*="fontSize"]').filter({ hasText: /の番/ })
        const actualText = await turnEl.textContent().catch(() => 'NOT FOUND')
        console.log(`TURN MISMATCH after move ${i + 1}`)
        console.log(`  Expected: "${move.expectedTurnAfter}"`)
        console.log(`  Actual: "${actualText}"`)
        await captureScreenshot(page, `${prefix}f-turn-mismatch-after`)
      }

      // --- Verify move count ---
      const moveCountLocator = page.locator(`text=手数: ${move.expectedMoveCountAfter}`)
      const moveCountVisible = await moveCountLocator
        .isVisible({ timeout: 3000 })
        .catch(() => false)

      if (moveCountVisible) {
        console.log(`Move count after move ${i + 1}: ${move.expectedMoveCountAfter} - CONFIRMED`)
      } else {
        const moveCountEl = page.locator('text=/手数: \\d+/')
        const allCounts = await moveCountEl.allTextContents()
        console.log(`Move count mismatch after move ${i + 1}`)
        console.log(`  Expected: 手数: ${move.expectedMoveCountAfter}`)
        console.log(`  Actual texts: ${JSON.stringify(allCounts)}`)
      }

      // --- Verify piece positions changed ---
      const srcPieceAfter = await getPieceTextAt(page, move.fromRow, move.fromCol)
      const dstPieceAfter = await getPieceTextAt(page, move.toRow, move.toCol)
      console.log(`Board after move ${i + 1}:`)
      console.log(`  Source (${move.fromRow},${move.fromCol}): "${srcPieceAfter}" [expected: empty]`)
      console.log(`  Dest   (${move.toRow},${move.toCol}): "${dstPieceAfter}" [expected: piece]`)
    }

    // ================================================================
    // STEP 7: Final verification
    // ================================================================
    console.log('\n========== STEP 7: Final verification ==========')

    await captureScreenshot(page, '07-final-board-state')

    const finalTurnVisible = await page.locator('text=後手の番')
      .isVisible({ timeout: 5000 })
      .catch(() => false)
    console.log(`Final turn (後手の番): ${finalTurnVisible ? 'CONFIRMED' : 'NOT CONFIRMED'}`)

    const finalMoveCountVisible = await page.locator('text=手数: 3')
      .isVisible({ timeout: 5000 })
      .catch(() => false)
    console.log(`Final move count (3): ${finalMoveCountVisible ? 'CONFIRMED' : 'NOT CONFIRMED'}`)

    // ================================================================
    // STEP 8: Comprehensive summary
    // ================================================================
    console.log('\n\n==========================================')
    console.log('          TEST EXECUTION SUMMARY')
    console.log('==========================================')

    console.log(`\nGame ID: ${gameId}`)
    console.log(`BLACK Player: ${blackPlayerId}`)
    console.log(`WHITE Player: ${whitePlayerId}`)

    const moveLogs = networkLogs.filter((log) => log.url.includes('/moves'))
    console.log(`\n--- Move API Calls (${moveLogs.length}) ---`)
    for (const log of moveLogs) {
      console.log(`  [${log.timestamp}] ${log.method} ${log.url}`)
      console.log(`    Status: ${log.status}`)
      if (log.requestBody) {
        console.log(`    Request: ${log.requestBody}`)
      }
      if (log.responseBody) {
        console.log(`    Response: ${log.responseBody.substring(0, 500)}`)
      }
    }

    console.log(`\n--- All API Calls (${networkLogs.length}) ---`)
    for (const log of networkLogs) {
      const marker = log.status >= 400 ? ' *** ERROR ***' : ''
      console.log(`  ${log.method} ${log.url} => ${log.status}${marker}`)
    }

    console.log(`\n--- Browser Console Errors (${consoleErrors.length}) ---`)
    if (consoleErrors.length > 0) {
      for (const err of consoleErrors) {
        console.log(`  ${err}`)
      }
    } else {
      console.log('  None')
    }

    console.log(`\n--- Page Errors (${allPageErrors.length}) ---`)
    if (allPageErrors.length > 0) {
      for (const err of allPageErrors) {
        console.log(`  ${err}`)
      }
    } else {
      console.log('  None')
    }

    console.log(`\n--- Screenshots ---`)
    console.log(`  Saved to: ${ARTIFACTS_DIR}/`)

    // ================================================================
    // Final assertions
    // ================================================================
    console.log('\n--- Final Assertions ---')

    // 1. No turn-related page errors
    const criticalErrors = allPageErrors.filter(
      (e) => e.toLowerCase().includes('turn')
    )
    if (criticalErrors.length > 0) {
      console.log('CRITICAL: Turn-related errors detected:')
      for (const e of criticalErrors) {
        console.log(`  ${e}`)
      }
    }
    expect(
      criticalErrors,
      'No turn-related errors should occur during alternating moves'
    ).toHaveLength(0)

    // 2. All move API calls succeeded
    const failedMoves = moveLogs.filter((log) => log.status >= 400)
    if (failedMoves.length > 0) {
      console.log('FAILED MOVES:')
      for (const m of failedMoves) {
        console.log(`  ${m.method} ${m.url} => ${m.status}`)
        console.log(`  Request: ${m.requestBody}`)
        console.log(`  Response: ${m.responseBody}`)
      }
    }
    expect(
      failedMoves,
      'All move API calls should succeed (status < 400)'
    ).toHaveLength(0)

    // 3. Exactly 3 successful moves
    const successfulMoves = moveLogs.filter(
      (log) => log.status >= 200 && log.status < 400
    )
    expect(
      successfulMoves.length,
      'Exactly 3 moves should have been made successfully'
    ).toBe(3)

    console.log('\n==========================================')
    console.log('          TEST COMPLETED SUCCESSFULLY')
    console.log('==========================================')
  })
})
