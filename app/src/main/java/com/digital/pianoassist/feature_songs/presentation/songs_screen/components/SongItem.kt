package com.digital.pianoassist.feature_songs.presentation.songs_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.digital.pianoassist.feature_songs.domain.model.Song

@Composable
fun SongItem(
    song: Song,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp, // default is set to 10 dp
    cutCornerSize: Dp = 30.dp,
    onInfoClick: (Song) -> Unit, // callback function to be triggered on info click
    last30DaysAverageScore: State<Double>
) {
    var isInfoVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        // matchParent size will first let the box get it's size from the text inside and then the
        // canvas will take the same size (to be adaptive for all screens)
        Canvas(modifier = Modifier.matchParentSize()) {
            // we define a path that goes around the rectangle with a corner cut
            val clipPath = Path().apply {
                lineTo(size.width - cutCornerSize.toPx(), 0f)
                lineTo(size.width, cutCornerSize.toPx())
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            clipPath(clipPath) {
                // what we draw in here it will make sure that out content is inside out path
                // so what we draw outside of the path will just be cut off (for eq the corner)
                drawRoundRect(
                    color = Color(0xFFF98866),
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
                // another small rectangle for the corner (half of it wil be cut off)
                // but this is used just to make the corner a bit darker (blending the colors)
                // we blend out rectangle color with black
                drawRoundRect(
                    color = Color(
                        ColorUtils.blendARGB(
                            Color(0xFFF98866).toArgb(),
                            Color.Black.toArgb(),
                            0.2f
                        )
                    ),
                    topLeft = Offset(size.width - cutCornerSize.toPx(), -100f),
                    size = Size(cutCornerSize.toPx() + 100f, cutCornerSize.toPx() + 100f),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(end = 32.dp) // to make sure the text doesn't overlap the info icon
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, // so we have just 1 line for the title
                overflow = TextOverflow.Ellipsis // so it is cut off when it gets too long
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = song.composer,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = {
                isInfoVisible = !isInfoVisible
                if (isInfoVisible) {
                    onInfoClick(song)// trigger the callback on the SongsScreen and then on the ViewModel
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "More information about"
            )
        }
    }
    AnimatedVisibility(
        visible = isInfoVisible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Column {
            SongInfoRubric(
                text = "The max score is:" + song.maxScore
            )
            SongInfoRubric(
                text = "The LAST 30 DAYS SCORE is: ${last30DaysAverageScore.value}"
            )
        }

    }
}