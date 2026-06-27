package com.ozansan.timewheel

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Cylinder radius of the wheel, expressed in item-heights. The rows wrap onto a
 * drum of this radius, so a larger value = gentler curve. iOS' picker is subtle;
 * ~2 rows keeps edge items tilted but still readable instead of folding flat.
 */
internal const val DRUM_RADIUS_IN_ITEMS = 2f

/**
 * X-axis tilt of a row [offset] item-heights from the center band. Wraps the row
 * onto the drum (angle = arc length / radius), clamped at 90deg so its back face
 * never shows. Shared so screenshot previews render the same curve the wheel does.
 */
internal fun drumRotationX(offset: Float): Float =
    Math.toDegrees((offset / DRUM_RADIUS_IN_ITEMS).toDouble())
        .toFloat()
        .coerceIn(-90f, 90f)

/** Fade applied to a row [distance] item-heights from center (1 at center, floored). */
internal fun drumAlpha(distance: Float): Float =
    (1f - distance * 0.28f).coerceIn(0.25f, 1f)

/**
 * iOS-style scrolling time picker.
 *
 * Two snapping wheels (hour and minute) with a highlighted center selection,
 * an alpha falloff and a 3D drum tilt away from center.
 *
 * @param onTimeChange invoked with (hour, minute) whenever the selection settles.
 */
@Composable
fun TimeWheel(
    modifier: Modifier = Modifier,
    initialHour: Int = 0,
    initialMinute: Int = 0,
    is24Hour: Boolean = true,
    visibleCount: Int = 5,
    itemHeight: Dp = 40.dp,
    onTimeChange: (hour: Int, minute: Int) -> Unit = { _, _ -> },
) {
    val hourCount = if (is24Hour) 24 else 12
    val rowHeight = itemHeight * visibleCount

    var hour = initialHour.coerceIn(0, hourCount - 1)
    var minute = initialMinute.coerceIn(0, 59)

    Box(modifier = modifier.height(rowHeight), contentAlignment = Alignment.Center) {
        // Center selection highlight band.
        Box(
            Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x14000000))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WheelColumn(
                modifier = Modifier.width(72.dp),
                count = hourCount,
                initialIndex = hour,
                visibleCount = visibleCount,
                itemHeight = itemHeight,
                label = { v -> if (is24Hour) "%02d".format(v) else "%d".format(if (v == 0) 12 else v) },
            ) { selected ->
                hour = selected
                onTimeChange(hour, minute)
            }

            Box(
                Modifier.width(16.dp).height(itemHeight),
                contentAlignment = Alignment.Center,
            ) {
                WheelText(":", alpha = 1f)
            }

            WheelColumn(
                modifier = Modifier.width(72.dp),
                count = 60,
                initialIndex = minute,
                visibleCount = visibleCount,
                itemHeight = itemHeight,
                label = { v -> "%02d".format(v) },
            ) { selected ->
                minute = selected
                onTimeChange(hour, minute)
            }
        }
    }
}

/** A single snapping wheel over [count] integer values. */
@Composable
private fun WheelColumn(
    count: Int,
    initialIndex: Int,
    visibleCount: Int,
    itemHeight: Dp,
    modifier: Modifier = Modifier,
    label: (Int) -> String,
    onSelect: (Int) -> Unit,
) {
    val half = visibleCount / 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    // Continuous centered value, in item units. The [half] blank padding rows make
    // firstVisibleItemIndex == the value sitting in the center band, so adding the
    // fractional scroll offset tracks the wheel position smoothly mid-scroll. This
    // drives the per-row drum rotation/falloff; rounding it gives the settled value.
    val centerFraction by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset / itemHeightPx
        }
    }
    val centerIndex by remember { derivedStateOf { centerFraction.roundToInt() } }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling) onSelect(centerIndex.coerceIn(0, count - 1))
            }
    }

    LazyColumn(
        modifier = modifier.height(itemHeight * visibleCount),
        state = listState,
        flingBehavior = flingBehavior,
    ) {
        items(half) { Spacer(itemHeight) }
        items(count) { value ->
            // Signed offset from the center band, in item units: negative above, positive below.
            val offset = value - centerFraction
            Box(
                Modifier.height(itemHeight).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                WheelText(label(value), alpha = drumAlpha(abs(offset)), rotationX = drumRotationX(offset))
            }
        }
        items(half) { Spacer(itemHeight) }
    }
}

@Composable
internal fun WheelText(text: String, alpha: Float, rotationX: Float = 0f) {
    BasicText(
        text = text,
        style = TextStyle(
            color = Color.Black,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                this.rotationX = rotationX
                // Push the camera back past the default (8f) so tilted rows foreshorten
                // gently, matching iOS' shallow perspective rather than a hard squash.
                cameraDistance = 12f * density
            },
    )
}

@Composable
private fun Spacer(height: Dp) {
    Box(Modifier.height(height).fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun TimeWheelPreview() {
    TimeWheel(initialHour = 9, initialMinute = 41)
}
