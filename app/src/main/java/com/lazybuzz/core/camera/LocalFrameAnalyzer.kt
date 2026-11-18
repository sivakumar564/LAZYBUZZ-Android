package com.lazybuzz.core.camera

import android.content.Context
import android.media.Image
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.MediaImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import java.util.concurrent.atomic.AtomicBoolean

class LocalFrameAnalyzer(
    context: Context,
    private val onDetection: (HandDetection?) -> Unit,
    private val onError: (String) -> Unit,
) : ImageAnalysis.Analyzer, AutoCloseable {
    private val appContext = context.applicationContext
    private val hasReportedError = AtomicBoolean(false)
    @Volatile private var closed = false
    private var handLandmarker: HandLandmarker? = null

    fun initialize() {
        check(handLandmarker == null && !closed) { "Hand tracker is already initialized or closed." }
        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath(MODEL_ASSET).build())
            .setRunningMode(RunningMode.VIDEO)
            .setNumHands(1)
            .build()
        handLandmarker = HandLandmarker.createFromOptions(appContext, options)
    }

    override fun analyze(image: ImageProxy) {
        try {
            if (closed) return
            val source: Image = image.image ?: return
            val landmarker = handLandmarker ?: return
            val rotationDegrees = image.imageInfo.rotationDegrees
            val mpImage = MediaImageBuilder(source).build()
            try {
                val imageOptions = ImageProcessingOptions.builder()
                    .setRotationDegrees(rotationDegrees)
                    .build()
                val timestampMs = image.imageInfo.timestamp / NANOS_PER_MILLI
                val result = landmarker.detectForVideo(mpImage, imageOptions, timestampMs)
                val points = result.landmarks().firstOrNull()
                val rotated = rotationDegrees == 90 || rotationDegrees == 270
                onDetection(
                    points?.let { hand ->
                        HandDetection(
                            landmarks = HandLandmarks(hand.map { NormalizedLandmark(it.x(), it.y(), it.z()) }),
                            imageWidth = if (rotated) image.height else image.width,
                            imageHeight = if (rotated) image.width else image.height,
                        )
                    },
                )
            } finally {
                mpImage.close()
            }
        } catch (exception: RuntimeException) {
            if (hasReportedError.compareAndSet(false, true)) {
                onError("Hand tracking failed: ${exception.message ?: "MediaPipe could not process the camera frame."}")
            }
        } finally {
            image.close()
        }
    }

    override fun close() {
        closed = true
        handLandmarker?.close()
        handLandmarker = null
    }

    companion object {
        const val MODEL_ASSET = "hand_landmarker.task"
        val TARGET_SIZE = Size(640, 480)
        private const val NANOS_PER_MILLI = 1_000_000L
    }
}
