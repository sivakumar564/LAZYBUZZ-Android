package com.lazybuzz.core.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * Local-only frame handoff point for the tracking pipeline added in the next milestone.
 * The current milestone intentionally does no recognition and never retains or transmits frames.
 */
class LocalFrameAnalyzer : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        image.close()
    }
}
