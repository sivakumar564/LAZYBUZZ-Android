package com.lazybuzz.core.camera

data class NormalizedLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
)

data class HandLandmarks(val points: List<NormalizedLandmark>) {
    init {
        require(points.size == LANDMARK_COUNT) {
            "A hand must contain exactly $LANDMARK_COUNT landmarks."
        }
    }

    val thumbTip: NormalizedLandmark get() = points[THUMB_TIP]
    val indexFingerTip: NormalizedLandmark get() = points[INDEX_FINGER_TIP]
    val indexFingerJoints: List<NormalizedLandmark> get() = points.subList(INDEX_FINGER_MCP, INDEX_FINGER_TIP + 1)

    companion object {
        const val LANDMARK_COUNT = 21
        const val THUMB_TIP = 4
        const val INDEX_FINGER_MCP = 5
        const val INDEX_FINGER_TIP = 8
    }
}

data class HandDetection(
    val landmarks: HandLandmarks,
    val imageWidth: Int,
    val imageHeight: Int,
)
