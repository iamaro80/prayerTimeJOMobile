package jo.aliftaa.prayertimes.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jo.aliftaa.prayertimes.R
import jo.aliftaa.prayertimes.data.model.AzanTrack
import jo.aliftaa.prayertimes.notification.PrayerNotificationHelper

@Composable
fun AudioSelectorDialog(
    prayerTitle: String,
    currentTrackId: String,
    onDismiss: () -> Unit,
    onTrackSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedId by remember { mutableStateOf(currentTrackId) }
    var currentlyPlayingId by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun playTrack(track: AzanTrack) {
        mediaPlayer?.release()
        try {
            val uri = Uri.parse("android.resource://${context.packageName}/${track.rawResId}")
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
                start()
                setOnCompletionListener {
                    currentlyPlayingId = null
                }
            }
            mediaPlayer = player
            currentlyPlayingId = track.id
        } catch (e: Exception) {
            currentlyPlayingId = null
        }
    }

    fun stopTrack() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentlyPlayingId = null
    }

    AlertDialog(
        onDismissRequest = {
            stopTrack()
            onDismiss()
        },
        title = {
            Text(
                text = "${stringResource(R.string.sound_track)} - $prayerTitle",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(PrayerNotificationHelper.AVAILABLE_TRACKS) { track ->
                    val isSelected = track.id == selectedId
                    val isPlaying = track.id == currentlyPlayingId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedId = track.id
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedId = track.id }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(track.titleResId),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    stopTrack()
                                } else {
                                    playTrack(track)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) stringResource(R.string.stop_audio) else stringResource(R.string.preview_audio),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    stopTrack()
                    onTrackSelected(selectedId)
                }
            ) {
                Text(stringResource(R.string.select))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    stopTrack()
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
