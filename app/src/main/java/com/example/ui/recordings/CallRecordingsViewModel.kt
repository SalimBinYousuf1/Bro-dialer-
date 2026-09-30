package com.example.ui.recordings

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.CallRecording
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class CallRecordingsViewModel : ViewModel() {

    private val repository = SalimApplication.instance.callRecordingRepository
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    val searchQuery = MutableStateFlow("")

    val allRecordings: StateFlow<List<CallRecording>> = repository.getAllRecordings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRecordings: StateFlow<List<CallRecording>> = combine(allRecordings, searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter {
                it.displayName.lowercase().contains(q) || it.phoneNumber.contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activePlayingId = MutableStateFlow<Long?>(null)
    val activePlayingId: StateFlow<Long?> = _activePlayingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionSeconds = MutableStateFlow(0L)
    val currentPositionSeconds: StateFlow<Long> = _currentPositionSeconds.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun togglePlay(recording: CallRecording) {
        if (_activePlayingId.value == recording.id) {
            if (_isPlaying.value) {
                pausePlayback()
            } else {
                resumePlayback()
            }
        } else {
            startPlayback(recording)
        }
    }

    private fun startPlayback(recording: CallRecording) {
        stopPlayback()
        val file = File(recording.filePath)
        if (!file.exists()) return

        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            mp.playbackParams = mp.playbackParams.setSpeed(_playbackSpeed.value)
                        } catch (_: Exception) {}
                    }
                    mp.start()
                    _isPlaying.value = true
                    _activePlayingId.value = recording.id
                    _currentPositionSeconds.value = 0L

                    progressJob?.cancel()
                    progressJob = viewModelScope.launch {
                        while (isActive && _isPlaying.value) {
                            val pos = (mediaPlayer?.currentPosition ?: 0) / 1000L
                            _currentPositionSeconds.value = pos
                            delay(300)
                        }
                    }
                }
                setOnCompletionListener {
                    stopPlayback()
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (_: Exception) {
            stopPlayback()
        }
    }

    fun pausePlayback() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
        } catch (_: Exception) {}
    }

    fun resumePlayback() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
        } catch (_: Exception) {}
    }

    fun seekTo(seconds: Long) {
        try {
            mediaPlayer?.seekTo((seconds * 1000).toInt())
            _currentPositionSeconds.value = seconds
        } catch (_: Exception) {}
    }

    fun cyclePlaybackSpeed() {
        val nextSpeed = when (_playbackSpeed.value) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        _playbackSpeed.value = nextSpeed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let {
                    it.playbackParams = it.playbackParams.setSpeed(nextSpeed)
                }
            } catch (_: Exception) {}
        }
    }

    fun stopPlayback() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _activePlayingId.value = null
        _isPlaying.value = false
        _currentPositionSeconds.value = 0L
    }

    fun deleteRecording(recording: CallRecording) {
        if (_activePlayingId.value == recording.id) {
            stopPlayback()
        }
        viewModelScope.launch {
            repository.deleteRecording(recording)
        }
    }

    fun shareRecording(context: Context, recording: CallRecording) {
        try {
            val file = File(recording.filePath)
            if (!file.exists()) return
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Call Recording with ${recording.displayName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Call Recording").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Sharing handled gracefully
        }
    }

    override fun onCleared() {
        stopPlayback()
        super.onCleared()
    }
}
