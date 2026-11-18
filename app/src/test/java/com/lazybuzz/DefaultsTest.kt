package com.lazybuzz

import com.lazybuzz.core.GestureRuntime
import com.lazybuzz.core.RuntimeStatus
import com.lazybuzz.core.camera.HandLandmarks
import com.lazybuzz.core.camera.NormalizedLandmark
import com.lazybuzz.data.models.GestureSettings
import com.lazybuzz.navigation.Dest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultsTest {
    @Test fun fingerBendIsPrimaryAndForwardTapIsOptional() {
        val s = GestureSettings()
        assertTrue(s.bendClickEnabled)
        assertFalse(s.forwardTapEnabled)
    }

    @Test fun startsAtFifteenFpsWithSlowerIdleRate() {
        val s = GestureSettings()
        assertEquals(15, s.targetFps)
        assertTrue(s.idleFps < s.targetFps)
    }

    @Test fun pipelineStartsStopped() {
        assertEquals(RuntimeStatus.STOPPED, GestureRuntime.state.value.status)
    }

    @Test fun allElevenScreensHaveUniqueRoutes() {
        assertEquals(11, Dest.entries.size)
        assertEquals(Dest.entries.size, Dest.entries.map { it.route }.toSet().size)
    }

    @Test fun liveCameraPreviewIsNotAPlaceholderDestination() {
        assertEquals("preview", Dest.Preview.route)
        assertEquals("", Dest.Preview.milestone)
    }

    @Test fun handLandmarksExposeNormalizedThumbAndIndexTipsFromThe21PointSet() {
        val points = List(HandLandmarks.LANDMARK_COUNT) { index ->
            NormalizedLandmark(index / 20f, index / 40f, 0f)
        }
        val hand = HandLandmarks(points)

        assertEquals(21, hand.points.size)
        assertEquals(points[4], hand.thumbTip)
        assertEquals(points[8], hand.indexFingerTip)
        assertEquals(points.subList(5, 9), hand.indexFingerJoints)
        assertEquals(0.4f, hand.indexFingerTip.x, 0f)
        assertEquals(0.2f, hand.indexFingerTip.y, 0f)
    }
}
