package com.ozansan.timewheel

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import org.junit.Rule
import org.junit.Test

/**
 * Records the animated README preview. Paparazzi advances the Compose frame clock
 * frame-by-frame, so the wheel is driven by an [rememberInfiniteTransition] over its
 * center position rather than real fling/scroll (which the frame clock can't replay).
 * The rows reuse [WheelText] + [drumRotationX]/[drumAlpha], so the recording matches
 * the shipping [TimeWheel] pixel for pixel.
 *
 * Run `./gradlew :timewheel:recordPaparazziDebug` to (re)generate the APNG under
 * `src/test/snapshots/`.
 */
class TimeWheelSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        // Shrink the canvas to the composable so the GIF is a tight component card.
        renderingMode = RenderingMode.SHRINK,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    @Test
    fun drum() {
        // alpha05 only exposes the View gif() overload, so host the composable in a
        // ComposeView. A 2s tween reversed = a seamless 4s loop matching the window.
        val view = ComposeView(paparazzi.context).apply {
            setContent { AnimatedTimeWheel() }
        }
        paparazzi.gif(view, name = "timewheel", start = 0L, end = 4_000L, fps = 30)
    }
}

@Composable
private fun AnimatedTimeWheel(
    itemHeight: Dp = 40.dp,
    visibleCount: Int = 5,
) {
    val transition = rememberInfiniteTransition(label = "wheel")
    val spec = infiniteRepeatable<Float>(
        animation = tween(durationMillis = 2_000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
    )
    val hour by transition.animateFloat(initialValue = 9f, targetValue = 11f, animationSpec = spec, label = "hour")
    val minute by transition.animateFloat(initialValue = 41f, targetValue = 48f, animationSpec = spec, label = "minute")

    Box(Modifier.background(Color.White).padding(horizontal = 24.dp, vertical = 16.dp)) {
        Box(
            modifier = Modifier.height(itemHeight * visibleCount),
            contentAlignment = Alignment.Center,
        ) {
            // Center selection highlight band, matching TimeWheel.
            Box(
                Modifier
                    .width(160.dp)
                    .height(itemHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x14000000))
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DrumColumn(center = hour, count = 24, width = 72.dp, itemHeight = itemHeight, visibleCount = visibleCount) {
                    "%02d".format(it)
                }
                Box(Modifier.width(16.dp).height(itemHeight), contentAlignment = Alignment.Center) {
                    WheelText(":", alpha = 1f)
                }
                DrumColumn(center = minute, count = 60, width = 72.dp, itemHeight = itemHeight, visibleCount = visibleCount) {
                    "%02d".format(it)
                }
            }
        }
    }
}

/** Static-layout drum column placing rows by their offset from [center] (a fractional value index). */
@Composable
private fun DrumColumn(
    center: Float,
    count: Int,
    width: Dp,
    itemHeight: Dp,
    visibleCount: Int,
    label: (Int) -> String,
) {
    val half = visibleCount / 2
    Box(
        modifier = Modifier.width(width).height(itemHeight * visibleCount),
        contentAlignment = Alignment.Center,
    ) {
        val firstValue = floor(center).toInt() - half - 1
        val lastValue = ceil(center).toInt() + half + 1
        for (value in firstValue..lastValue) {
            if (value < 0 || value >= count) continue
            val offset = value - center
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth()
                    .offset(y = itemHeight * offset),
                contentAlignment = Alignment.Center,
            ) {
                WheelText(label(value), alpha = drumAlpha(abs(offset)), rotationX = drumRotationX(offset))
            }
        }
    }
}
