package com.example.telephony

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import com.example.SalimApplication
import com.example.data.model.CallRecording
import com.example.domain.usecase.PhoneNumberHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

object CallRecorder {

    private const val TAG = "CallRecorder"
    private val recordingLock = Any()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var timerJob: Job? = null

    private var currentNumber: String = ""
    private var currentDisplayName: String = ""
    private var currentCallId: String = ""
    private var startTimestamp: Long = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    fun startRecording(
        context: Context,
        number: String,
        displayName: String,
        callId: String
    ): Boolean = synchronized(recordingLock) {
        if (_isRecording.value) return true

        val cleanNumber = PhoneNumberHelper.normalizeNumber(number).ifBlank { "unknown" }
        currentNumber = number
        currentDisplayName = displayName.ifBlank { PhoneNumberHelper.formatForDisplay(number) }
        currentCallId = callId
        startTimestamp = System.currentTimeMillis()

        try {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS) ?: context.filesDir
            val recDir = File(baseDir, "call_recordings").apply { if (!exists()) mkdirs() }
            val outputFile = File(recDir, "call_${cleanNumber}_${startTimestamp}.m4a")
            currentFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            var configured = false
            // Try VOICE_COMMUNICATION first
            try {
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setAudioEncodingBitRate(128000)
                recorder.setAudioSamplingRate(44100)
                recorder.setOutputFile(outputFile.absolutePath)
                recorder.prepare()
                recorder.start()
                configured = true
            } catch (e: Exception) {
                Log.w(TAG, "VOICE_COMMUNICATION source failed, falling back to MIC: ${e.message}")
                try {
                    recorder.reset()
                } catch (_: Exception) {}
            }

            if (!configured) {
                // Fallback to MIC
                try {
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    recorder.setAudioEncodingBitRate(128000)
                    recorder.setAudioSamplingRate(44100)
                    recorder.setOutputFile(outputFile.absolutePath)
                    recorder.prepare()
                    recorder.start()
                    configured = true
                } catch (e: Exception) {
                    Log.e(TAG, "MIC fallback also failed: ${e.message}")
                    try {
                        recorder.release()
                    } catch (_: Exception) {}
                    return false
                }
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _durationSeconds.value = 0L

            timerJob?.cancel()
            timerJob = scope.launch {
                while (isActive && _isRecording.value) {
                    delay(1000)
                    _durationSeconds.value += 1
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            cancelRecording()
            return false
        }
    }

    fun stopRecording(): CallRecording? = synchronized(recordingLock) {
        if (!_isRecording.value) return null

        timerJob?.cancel()
        timerJob = null
        _isRecording.value = false

        val duration = _durationSeconds.value
        val file = currentFile

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (_: Exception) {}
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null

        if (file != null && file.exists() && file.length() >= 512L) {
            val record = CallRecording(
                callId = currentCallId,
                phoneNumber = currentNumber,
                displayName = currentDisplayName,
                timestamp = startTimestamp,
                durationSeconds = duration,
                filePath = file.absolutePath,
                fileSizeBytes = file.length()
            )

            // Save to Room DB asynchronously
            scope.launch(Dispatchers.IO) {
                try {
                    SalimApplication.instance.callRecordingRepository.saveRecording(record)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving recording to Room: ${e.message}")
                }
            }

            currentFile = null
            _durationSeconds.value = 0L
            return record
        } else {
            file?.delete()
            currentFile = null
            _durationSeconds.value = 0L
            return null
        }
    }

    fun cancelRecording() {
        timerJob?.cancel()
        timerJob = null
        _isRecording.value = false
        _durationSeconds.value = 0L

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (_: Exception) {}
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null

        try {
            currentFile?.delete()
        } catch (_: Exception) {}
        currentFile = null
    }
}
