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

package app.ss.media.playback.ui.nowPlaying

import android.view.MotionEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.media.R
import app.ss.media.playback.extensions.NONE_PLAYBACK_STATE
import app.ss.media.playback.extensions.isPlaying
import app.ss.models.media.AudioFile
import app.ss.media.playback.model.PlaybackQueue
import app.ss.media.playback.model.PlaybackSpeed
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import app.ss.media.playback.ui.nowPlaying.ScreenDefaults.tintColor
import app.ss.media.playback.ui.nowPlaying.components.BoxState
import app.ss.media.playback.ui.nowPlaying.components.CoverImage
import app.ss.media.playback.ui.nowPlaying.components.NowPlayingColumn
import app.ss.media.playback.ui.nowPlaying.components.PlayBackControls
import app.ss.media.playback.ui.nowPlaying.components.PlaybackProgress
import app.ss.media.playback.ui.nowPlaying.components.PlaybackQueueList
import app.ss.media.playback.ui.playbackContentColor
import com.cryart.design.theme.BaseBlue
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.Spacing32
import com.cryart.design.theme.Spacing8
import com.cryart.design.theme.TitleMedium
import com.cryart.design.widgets.DragHandle
import kotlinx.coroutines.flow.StateFlow

private object ScreenDefaults {

    @Composable
    fun tintColor(): Color =
        if (isSystemInDarkTheme()) {
            BaseGrey2
        } else {
            BaseBlue
        }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun NowPlayingScreen(
    viewModel: NowPlayingViewModel = viewModel(),
    isDraggable: (Boolean) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val playbackConnection = viewModel.playbackConnection
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
        .collectAsState(NONE_PLAYBACK_STATE)
    val nowPlaying by rememberFlowWithLifecycle(viewModel.nowPlayingAudio)
        .collectAsState(AudioFile(""))
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue)
        .collectAsState(PlaybackQueue())
    val nowPlayingAudio = if (nowPlaying.id.isEmpty()) {
        playbackQueue.currentAudio ?: nowPlaying
    } else {
        nowPlaying
    }

    var boxState by remember { mutableStateOf(BoxState.Expanded) }
    val expanded = boxState == BoxState.Expanded

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                isDraggable(false)
                return super.onPreScroll(available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                isDraggable(true)
                return super.onPostFling(consumed, available)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = Spacing16)
            .nestedScroll(connection = nestedScrollConnection),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var heightState by remember { mutableStateOf(HeightState()) }

        DragHandle()

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    heightState = heightState.copy(
                        container = coordinates.size.height
                    )
                }
        ) {
            val marginTop by animateDpAsState(
                if (expanded) 0.dp else Dimens.grid_2
            )

            val constrains = decoupledConstraints(
                expanded = expanded,
                marginTop = marginTop
            )
            ConstraintLayout(
                constraintSet = constrains,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = Dimens.grid_4
                    )
            ) {

                CoverImage(
                    audio = nowPlayingAudio,
                    boxState = boxState,
                    modifier = Modifier
                        .layoutId("image")
                        .onGloballyPositioned { imageCoordinates ->
                            heightState = heightState.copy(
                                image = imageCoordinates.size.height
                            )
                        }
                )

                NowPlayingColumn(
                    audio = nowPlayingAudio,
                    boxState = boxState,
                    modifier = Modifier.layoutId("text")
                )

                PlaybackQueueList(
                    listState = listState,
                    modifier = Modifier
                        .alpha(if (expanded) 0f else 1f)
                        .padding(top = Spacing16)
                        .height(
                            with(LocalDensity.current) {
                                (heightState.container - heightState.image).toDp()
                            }
                        )
                        .pointerInteropFilter { event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    if (listState.firstVisibleItemIndex > 0) {
                                        isDraggable(false)
                                    }
                                }
                                MotionEvent.ACTION_UP -> {
                                    isDraggable(true)
                                }
                            }
                            false
                        }
                        .layoutId("queue"),
                    playbackQueue = playbackQueue.audiosList,
                    nowPlayingId = nowPlaying.id,
                    isPlaying = playbackState.isPlaying,
                    onPlayAudio = { position ->
                        playbackConnection.transportControls?.skipToQueueItem(position.toLong())
                    }
                )
            }
        }

        val spacing by animateDpAsState(
            if (expanded) Spacing32 else 0.dp
        )

        PlaybackProgress(
            playbackState = playbackState,
            playbackConnection = playbackConnection
        )

        Spacer(modifier = Modifier.height(spacing))

        PlayBackControls(
            playbackState = playbackState,
            contentColor = playbackContentColor(),
            playbackConnection = playbackConnection
        )

        Spacer(modifier = Modifier.height(spacing))

        BottomControls(
            playbackSpeedFlow = playbackConnection.playbackSpeed,
            toggleSpeed = { playbackSpeed ->
                playbackConnection.toggleSpeed(playbackSpeed)
            },
            toggleExpand = {
                boxState = when (boxState) {
                    BoxState.Collapsed -> BoxState.Expanded
                    BoxState.Expanded -> BoxState.Collapsed
                }
            }
        )
    }
}

private data class HeightState(
    val container: Int = 0,
    val image: Int = 0
)

private fun decoupledConstraints(
    expanded: Boolean,
    marginTop: Dp = 0.dp
): ConstraintSet {
    return ConstraintSet {
        val image = createRefFor("image")
        val text = createRefFor("text")
        val queue = createRefFor("queue")
        val endGuideline = createGuidelineFromEnd(Spacing16)

        if (expanded) {
            createVerticalChain(image, text, chainStyle = ChainStyle.Spread)
        }

        constrain(image) {
            if (expanded) {
                centerHorizontallyTo(parent)
            } else {
                top.linkTo(parent.top, margin = marginTop)
                start.linkTo(parent.start)
            }
        }

        constrain(text) {
            if (expanded) {
                centerHorizontallyTo(parent)
            } else {
                top.linkTo(image.top)
                bottom.linkTo(image.bottom)
                start.linkTo(image.end)
                end.linkTo(endGuideline)
                width = Dimension.fillToConstraints
            }
        }

        constrain(queue) {
            centerHorizontallyTo(parent)
            top.linkTo(image.bottom, margin = Spacing16)
            bottom.linkTo(parent.bottom, margin = Spacing16)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BottomControls(
    modifier: Modifier = Modifier,
    playbackSpeedFlow: StateFlow<PlaybackSpeed>,
    toggleSpeed: (PlaybackSpeed) -> Unit = {},
    toggleExpand: () -> Unit = {}
) {
    val playbackSpeed by rememberFlowWithLifecycle(playbackSpeedFlow)
        .collectAsState(PlaybackSpeed.NORMAL)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = Spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = {
                toggleSpeed(playbackSpeed)
            }
        ) {
            AnimatedContent(
                targetState = playbackSpeed,
                transitionSpec = {
                    if (targetState.speed > initialState.speed) {
                        slideInVertically({ height -> height }) + fadeIn() with
                            slideOutVertically({ height -> -height }) + fadeOut()
                    } else {
                        slideInVertically({ height -> -height }) + fadeIn() with
                            slideOutVertically({ height -> height }) + fadeOut()
                    }.using(
                        SizeTransform(clip = false)
                    )
                }
            ) { targetSpeed ->
                Text(
                    text = targetSpeed.label,
                    style = TitleMedium.copy(
                        color = tintColor()
                    )
                )
            }
        }

        IconButton(onClick = toggleExpand) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_icon_playlist),
                contentDescription = "PlayList",
                tint = tintColor()
            )
        }
    }
}