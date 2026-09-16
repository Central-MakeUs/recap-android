package com.chalkak.recap.app.notification

import app.cash.turbine.test
import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotOrganizeOutcome
import com.chalkak.recap.core.model.capture.OrganizeStatus
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OrganizeTerminalResultMapperTest {
    @Test
    fun `fromRemote maps completed to all success`() {
        val result = OrganizeTerminalResultMapper.fromRemote(
            ScreenshotOrganizeOutcome.RemoteCompleted(
                successCount = 3,
                failCount = 0,
                status = OrganizeStatus.COMPLETED,
            ),
        )

        assertEquals(OrganizeTerminalResult.AllSuccess(successCount = 3), result)
    }

    @Test
    fun `fromRemote maps partial failed to partial success`() {
        val result = OrganizeTerminalResultMapper.fromRemote(
            ScreenshotOrganizeOutcome.RemoteCompleted(
                successCount = 2,
                failCount = 1,
                status = OrganizeStatus.PARTIAL_FAILED,
            ),
        )

        assertEquals(
            OrganizeTerminalResult.PartialSuccess(successCount = 2, failCount = 1),
            result,
        )
    }

    @Test
    fun `fromRemote maps failed and cancelled to all failed`() {
        assertEquals(
            OrganizeTerminalResult.AllFailed(),
            OrganizeTerminalResultMapper.fromRemote(
                ScreenshotOrganizeOutcome.RemoteCompleted(
                    successCount = 0,
                    failCount = 2,
                    status = OrganizeStatus.FAILED,
                ),
            ),
        )
        assertEquals(
            OrganizeTerminalResult.AllFailed(),
            OrganizeTerminalResultMapper.fromRemote(
                ScreenshotOrganizeOutcome.RemoteCompleted(
                    successCount = 0,
                    failCount = 0,
                    status = OrganizeStatus.CANCELLED,
                ),
            ),
        )
    }

    @Test
    fun `fromLocalPersisted maps save failure with partial progress`() {
        assertEquals(
            OrganizeTerminalResult.PartialSuccess(successCount = 1, failCount = 2),
            OrganizeTerminalResultMapper.fromLocalPersisted(
                persistedCount = 1,
                totalCount = 3,
                saveFailed = true,
            ),
        )
        assertEquals(
            OrganizeTerminalResult.AllFailed(),
            OrganizeTerminalResultMapper.fromLocalPersisted(
                persistedCount = 0,
                totalCount = 3,
                saveFailed = true,
            ),
        )
    }
}

class OrganizeProgressTrackerTest {
    @Test
    fun `onStarted marks running and returns run id`() {
        val tracker = OrganizeProgressTracker()

        val runId = tracker.onStarted(totalCount = 4)

        assertTrue(runId > 0)
        assertEquals(
            OrganizeProgressSnapshot(isRunning = true, completedCount = 0, totalCount = 4),
            tracker.snapshot.value,
        )
    }

    @Test
    fun `stale run id does not overwrite newer run`() = runTest {
        val tracker = OrganizeProgressTracker()
        val firstRun = tracker.onStarted(2)
        val secondRun = tracker.onStarted(5)

        tracker.onProgress(firstRun, completedCount = 2, totalCount = 2)
        tracker.onCancelled(firstRun)

        assertEquals(
            OrganizeProgressSnapshot(isRunning = true, completedCount = 0, totalCount = 5),
            tracker.snapshot.value,
        )

        tracker.terminalResults.test {
            tracker.onTerminal(firstRun, OrganizeTerminalResult.AllFailed())
            expectNoEvents()
            tracker.onTerminal(secondRun, OrganizeTerminalResult.AllSuccess(5))
            assertEquals(OrganizeTerminalResult.AllSuccess(5), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(tracker.snapshot.value.isRunning)
    }
}

class OrganizeNotificationPolicyTest {
    @Test
    fun `progress service is shown only while running and preference enabled`() {
        assertTrue(
            shouldShowProgress(
                isRunning = true,
                organizeCompleteNotificationEnabled = true,
            ),
        )
        assertFalse(
            shouldShowProgress(
                isRunning = true,
                organizeCompleteNotificationEnabled = false,
            ),
        )
        assertFalse(
            shouldShowProgress(
                isRunning = false,
                organizeCompleteNotificationEnabled = true,
            ),
        )
    }

    @Test
    fun `terminal notification is suppressed while app is foreground`() {
        assertFalse(
            shouldNotifyTerminal(
                isAppInForeground = true,
                organizeCompleteNotificationEnabled = true,
                notificationsEnabled = true,
            ),
        )
    }

    @Test
    fun `terminal notification is sent while app is background and enabled`() {
        assertTrue(
            shouldNotifyTerminal(
                isAppInForeground = false,
                organizeCompleteNotificationEnabled = true,
                notificationsEnabled = true,
            ),
        )
    }

    @Test
    fun `terminal notification is suppressed when preference or permission is disabled`() {
        assertFalse(
            shouldNotifyTerminal(
                isAppInForeground = false,
                organizeCompleteNotificationEnabled = false,
                notificationsEnabled = true,
            ),
        )
        assertFalse(
            shouldNotifyTerminal(
                isAppInForeground = false,
                organizeCompleteNotificationEnabled = true,
                notificationsEnabled = false,
            ),
        )
    }
}
