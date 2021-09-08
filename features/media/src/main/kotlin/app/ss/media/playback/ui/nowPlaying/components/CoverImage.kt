/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.media.playback.ui.nowPlaying.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.media.playback.model.AudioFile
import app.ss.media.playback.ui.common.RemoteImage
import coil.annotation.ExperimentalCoilApi
import com.cryart.design.theme.Dimens

private interface Sizes {
    val width: Dp
    val height: Dp
}

private enum class CoverOrientation(val key: String) : Sizes {
    SQUARE("square") {
        override val width: Dp = 276.dp
        override val height: Dp = 276.dp
    },
    PORTRAIT("portrait") {
        override val width: Dp = 187.dp
        override val height: Dp = 276.dp
    };

    companion object {
        private val map = values().associateBy(CoverOrientation::key)

        fun fromKey(type: String) = map[type]
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun CoverImage(
    modifier: Modifier = Modifier,
    audio: AudioFile,
    boxState: BoxState = BoxState.Expanded,
) {
    val orientation = CoverOrientation.fromKey(audio.imageRatio) ?: CoverOrientation.PORTRAIT
    val collapsed = boxState == BoxState.Collapsed
    val width by animateDpAsState(
        targetValue = if (collapsed) orientation.width / 2 else orientation.width,
    )
    val height by animateDpAsState(
        targetValue = if (collapsed) orientation.height / 2 else orientation.height,
    )

    RemoteImage(
        data = audio.image,
        modifier = modifier
            .size(
                width = width,
                height = height
            )
            .padding(Dimens.grid_1),
        contentDescription = audio.title,
        cornerRadius = CoverCornerRadius,
    )
}

private const val CoverCornerRadius = 6f
