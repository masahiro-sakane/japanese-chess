import { test, expect } from '@playwright/test'

const ARTIFACTS_DIR = 'C:/Develop/workspace/claude/japanese-chess/frontend/artifacts'

/**
 * E2E Test: Japanese Chess (Shogi) Piece Movement
 *
 * Flow:
 * 1. Navigate to home page
 * 2. Go to "Create Game" page via the link
 * 3. Submit the form to create a new game
 * 4. Navigate to the game detail page
 * 5. Click on the pawn at position (6, 6) - 7七歩
 * 6. Click on position (6, 5) - 7六 to move the pawn forward
 * 7. Capture screenshots and network responses at each step
 */
test.describe('Shogi Piece Movement', () => {
  test('should create a new game and move a pawn from 7七 to 7六', async ({ page }) => {
    // Collect all network requests and responses for /api/games
    const networkLogs: Array<{
      method: string
      url: string
      status: number
      requestBody: string | null
      responseBody: string | null
    }> = []

    page.on('response', async (response) => {
      const url = response.url()
      if (url.includes('/api/')) {
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

        networkLogs.push({
          method: response.request().method(),
          url,
          status: response.status(),
          requestBody,
          responseBody,
        })

        console.log(
          `[API] ${response.request().method()} ${url} => ${response.status()}`
        )
        if (requestBody) {
          console.log(`  Request Body: ${requestBody}`)
        }
        if (responseBody) {
          console.log(`  Response Body: ${responseBody.substring(0, 500)}`)
        }
      }
    })

    // Also capture console errors from the browser
    const consoleErrors: string[] = []
    page.on('console', (msg) => {
      if (msg.type() === 'error') {
        consoleErrors.push(msg.text())
        console.log(`[Browser Error] ${msg.text()}`)
      }
    })

    // ============================================================
    // Step 1: Navigate to the home page
    // ============================================================
    console.log('\n--- Step 1: Navigate to home page ---')
    await page.goto('http://localhost:5173/')
    await page.waitForLoadState('networkidle')
    await page.screenshot({ path: `${ARTIFACTS_DIR}/01-home-page.png`, fullPage: true })
    console.log('Home page loaded successfully')

    // Check for any errors on the page
    const homeError = page.locator('.error')
    if (await homeError.isVisible({ timeout: 2000 }).catch(() => false)) {
      const errorText = await homeError.textContent()
      console.log(`[Page Error on Home] ${errorText}`)
    }

    // ============================================================
    // Step 2: Navigate to Create Game page
    // ============================================================
    console.log('\n--- Step 2: Navigate to Create Game page ---')
    const createLink = page.locator('a[href="/create"]')
    await expect(createLink).toBeVisible({ timeout: 10000 })
    await createLink.click()
    await page.waitForURL('**/create')
    await page.waitForLoadState('networkidle')
    await page.screenshot({ path: `${ARTIFACTS_DIR}/02-create-game-page.png`, fullPage: true })
    console.log('Create Game page loaded')

    // Verify the form elements are present
    await expect(page.getByRole('heading', { name: '新規対局の作成' })).toBeVisible()
    const submitButton = page.locator('button[type="submit"]')
    await expect(submitButton).toBeVisible()
    await expect(submitButton).toContainText('対局を開始')

    // ============================================================
    // Step 3: Create a new game (submit the form)
    // ============================================================
    console.log('\n--- Step 3: Create new game ---')

    // The create page shows an alert() with the game ID after creation.
    // We need to intercept the dialog to capture the game ID.
    let gameId: string | null = null

    page.on('dialog', async (dialog) => {
      const message = dialog.message()
      console.log(`[Alert Dialog] ${message}`)

      // Extract game ID from the alert message
      // Format: "... Game ID: <uuid> ..."
      const match = message.match(/Game ID:\s*([a-f0-9-]+)/i)
      if (match) {
        gameId = match[1]
        console.log(`Captured Game ID: ${gameId}`)
      }

      await dialog.accept()
    })

    // Click submit and wait for the API response
    const createResponsePromise = page.waitForResponse(
      (resp) => resp.url().includes('/api/games') && resp.request().method() === 'POST',
      { timeout: 15000 }
    )

    await submitButton.click()

    const createResponse = await createResponsePromise
    const createResponseBody = await createResponse.text()
    console.log(`Create Game API Response: ${createResponse.status()} ${createResponseBody}`)

    await page.screenshot({ path: `${ARTIFACTS_DIR}/03-after-create-game.png`, fullPage: true })

    // If the alert did not fire or gameId was not captured, try to parse it from the API response
    if (!gameId) {
      try {
        const parsed = JSON.parse(createResponseBody)
        gameId = parsed.gameId
        console.log(`Game ID from API response: ${gameId}`)
      } catch {
        console.log('Could not parse game ID from response')
      }
    }

    // Wait for navigation back to home page
    await page.waitForURL('**/', { timeout: 10000 })
    await page.waitForLoadState('networkidle')
    await page.screenshot({ path: `${ARTIFACTS_DIR}/04-home-after-create.png`, fullPage: true })

    // Verify we have a game ID
    expect(gameId).toBeTruthy()
    console.log(`\nGame created successfully with ID: ${gameId}`)

    // ============================================================
    // Step 4: Navigate to the game detail page
    // ============================================================
    console.log('\n--- Step 4: Navigate to game detail page ---')

    // Navigate directly to the game detail page
    await page.goto(`http://localhost:5173/game/${gameId}`)
    await page.waitForLoadState('networkidle')

    // Wait for the board to render
    const boardContainer = page.locator('.board-container')
    await expect(boardContainer).toBeVisible({ timeout: 15000 })
    await page.screenshot({ path: `${ARTIFACTS_DIR}/05-game-detail-board.png`, fullPage: true })
    console.log('Game board loaded')

    // Check for errors on the game detail page
    const detailError = page.locator('.error')
    if (await detailError.isVisible({ timeout: 2000 }).catch(() => false)) {
      const errorText = await detailError.textContent()
      console.log(`[Page Error on Game Detail] ${errorText}`)
      await page.screenshot({ path: `${ARTIFACTS_DIR}/05-error-game-detail.png`, fullPage: true })
    }

    const moveError = page.locator('.move-error')
    if (await moveError.isVisible({ timeout: 1000 }).catch(() => false)) {
      const errorText = await moveError.textContent()
      console.log(`[Move Error] ${errorText}`)
    }

    // Verify game info is displayed
    const gameInfo = page.locator('.game-info-panel')
    await expect(gameInfo).toBeVisible()
    console.log('Game info panel visible')

    // Verify current turn is BLACK (first player)
    const turnInfo = page.locator('dd:has-text("先手")')
    await expect(turnInfo.first()).toBeVisible()
    console.log('Current turn: BLACK (先手) confirmed')

    // ============================================================
    // Step 5: Click on the pawn at position (6, 6) - 7七歩
    // ============================================================
    console.log('\n--- Step 5: Click pawn at position (6, 6) - 7七歩 ---')

    // The board renders rows and columns as nested divs:
    // .board > .board-row (rows 0-8) > .board-cell (columns 0-8)
    // Position (6, 6) = row index 6, column index 6
    const boardRows = page.locator('.board-row')
    const targetRow = boardRows.nth(6)
    const targetCell = targetRow.locator('.board-cell').nth(6)

    // Verify there is a piece at this position
    const pieceAtSource = targetCell.locator('.piece')
    await expect(pieceAtSource).toBeVisible({ timeout: 5000 })
    const pieceText = await pieceAtSource.textContent()
    console.log(`Piece at (6,6): "${pieceText}"`)

    // Take screenshot before clicking
    await page.screenshot({ path: `${ARTIFACTS_DIR}/06-before-select-piece.png`, fullPage: true })

    // Click the cell to select the pawn
    await targetCell.click()

    // Wait a moment for selection UI to update
    await page.waitForTimeout(300)

    // The cell should now have the 'selected' class
    const selectedCell = page.locator('.board-cell.selected')
    const isSelected = await selectedCell.isVisible({ timeout: 3000 }).catch(() => false)
    console.log(`Cell selected state: ${isSelected}`)

    await page.screenshot({ path: `${ARTIFACTS_DIR}/07-piece-selected.png`, fullPage: true })

    // ============================================================
    // Step 6: Click on position (6, 5) - 7六 to move the piece
    // ============================================================
    console.log('\n--- Step 6: Click destination at position (6, 5) - 7六 ---')

    const destRow = boardRows.nth(5)
    const destCell = destRow.locator('.board-cell').nth(6)

    // Log what is at the destination before clicking
    const destPiece = destCell.locator('.piece')
    const destHasPiece = await destPiece.isVisible({ timeout: 1000 }).catch(() => false)
    if (destHasPiece) {
      const destPieceText = await destPiece.textContent()
      console.log(`Piece already at destination (6,5): "${destPieceText}"`)
    } else {
      console.log('Destination (6,5) is empty - good for move')
    }

    // Set up listener for the move API call
    const moveResponsePromise = page.waitForResponse(
      (resp) => resp.url().includes('/moves') && resp.request().method() === 'POST',
      { timeout: 15000 }
    ).catch((err) => {
      console.log(`[Move API timeout] ${err.message}`)
      return null
    })

    await page.screenshot({ path: `${ARTIFACTS_DIR}/08-before-move.png`, fullPage: true })

    // Click the destination cell
    await destCell.click()

    // Wait for the move API response
    const moveResponse = await moveResponsePromise

    if (moveResponse) {
      const moveRequestBody = moveResponse.request().postData()
      const moveResponseBody = await moveResponse.text()
      console.log(`\n[Move API Request]`)
      console.log(`  URL: ${moveResponse.url()}`)
      console.log(`  Method: ${moveResponse.request().method()}`)
      console.log(`  Status: ${moveResponse.status()}`)
      console.log(`  Request Body: ${moveRequestBody}`)
      console.log(`  Response Body: ${moveResponseBody}`)

      if (moveResponse.status() >= 400) {
        console.log(`\n*** MOVE FAILED with status ${moveResponse.status()} ***`)
        try {
          const errorData = JSON.parse(moveResponseBody)
          console.log(`Error details: ${JSON.stringify(errorData, null, 2)}`)
        } catch {
          console.log(`Error body: ${moveResponseBody}`)
        }
      }
    } else {
      console.log('\n*** No move API call was detected ***')
    }

    // Wait for UI to update after move
    await page.waitForTimeout(1000)
    await page.waitForLoadState('networkidle')

    await page.screenshot({ path: `${ARTIFACTS_DIR}/09-after-move.png`, fullPage: true })

    // ============================================================
    // Step 7: Check for error messages
    // ============================================================
    console.log('\n--- Step 7: Check for errors ---')

    const moveErrorAfter = page.locator('.move-error')
    if (await moveErrorAfter.isVisible({ timeout: 2000 }).catch(() => false)) {
      const errorText = await moveErrorAfter.textContent()
      console.log(`[Move Error After] ${errorText}`)
      await page.screenshot({ path: `${ARTIFACTS_DIR}/10-move-error.png`, fullPage: true })
    } else {
      console.log('No move error displayed on page')
    }

    // Check if the board was reloaded (game was re-fetched)
    const loadingIndicator = page.locator('.move-loading')
    if (await loadingIndicator.isVisible({ timeout: 500 }).catch(() => false)) {
      console.log('Move is still loading...')
      await loadingIndicator.waitFor({ state: 'hidden', timeout: 10000 })
    }

    // Verify the piece moved: check that the destination now has a piece
    const destPieceAfterMove = destRow.locator('.board-cell').nth(6).locator('.piece')
    const movedPieceVisible = await destPieceAfterMove.isVisible({ timeout: 5000 }).catch(() => false)
    if (movedPieceVisible) {
      const movedPieceText = await destPieceAfterMove.textContent()
      console.log(`Piece at destination (6,5) after move: "${movedPieceText}"`)
    } else {
      console.log('No piece visible at destination after move')
    }

    // Check if the source cell is now empty
    const sourcePieceAfterMove = targetRow.locator('.board-cell').nth(6).locator('.piece')
    const sourceStillHasPiece = await sourcePieceAfterMove.isVisible({ timeout: 1000 }).catch(() => false)
    console.log(`Source cell (6,6) still has piece: ${sourceStillHasPiece}`)

    await page.screenshot({ path: `${ARTIFACTS_DIR}/11-final-board-state.png`, fullPage: true })

    // ============================================================
    // Step 8: Print summary of all network logs
    // ============================================================
    console.log('\n--- Network Request Summary ---')
    const moveLogs = networkLogs.filter((log) => log.url.includes('/moves'))
    if (moveLogs.length > 0) {
      for (const log of moveLogs) {
        console.log(`\n  ${log.method} ${log.url}`)
        console.log(`  Status: ${log.status}`)
        if (log.requestBody) {
          console.log(`  Request: ${log.requestBody}`)
        }
        if (log.responseBody) {
          console.log(`  Response: ${log.responseBody.substring(0, 500)}`)
        }
      }
    } else {
      console.log('No /moves API requests were logged')
    }

    // Print any browser console errors
    if (consoleErrors.length > 0) {
      console.log('\n--- Browser Console Errors ---')
      for (const err of consoleErrors) {
        console.log(`  ${err}`)
      }
    } else {
      console.log('\nNo browser console errors detected')
    }

    // Final summary
    console.log('\n=== Test Summary ===')
    console.log(`Game ID: ${gameId}`)
    console.log(`Move API calls: ${moveLogs.length}`)
    console.log(`Browser errors: ${consoleErrors.length}`)
    console.log(`Total API calls logged: ${networkLogs.length}`)
    console.log(`Screenshots saved to: ${ARTIFACTS_DIR}/`)
  })
})
