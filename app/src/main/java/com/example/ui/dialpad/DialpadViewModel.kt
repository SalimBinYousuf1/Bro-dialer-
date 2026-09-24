package com.example.ui.dialpad

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.ContactItem
import com.example.data.model.DialerSettings
import com.example.data.repository.ContactsRepository
import com.example.data.repository.TelecomRepository
import com.example.domain.usecase.PhoneNumberHelper
import com.example.domain.usecase.T9SearchEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DialpadViewModel(
    private val contactsRepository: ContactsRepository = SalimApplication.instance.contactsRepository,
    private val telecomRepository: TelecomRepository = SalimApplication.instance.telecomRepository,
    context: Context = SalimApplication.instance.applicationContext
) : ViewModel() {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_DTMF, 60)
    } catch (_: Exception) {
        null
    }

    val settings: StateFlow<DialerSettings> = SalimApplication.instance.preferencesManager
        .settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DialerSettings())

    private val _enteredNumber = MutableStateFlow("")
    val enteredNumber: StateFlow<String> = _enteredNumber.asStateFlow()

    private val _allContacts = MutableStateFlow<List<ContactItem>>(emptyList())
    private val _matchedContacts = MutableStateFlow<List<T9SearchEngine.T9MatchResult>>(emptyList())
    val matchedContacts: StateFlow<List<T9SearchEngine.T9MatchResult>> = _matchedContacts.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            val contacts = contactsRepository.loadContacts()
            _allContacts.value = contacts
            updateT9Matches(_enteredNumber.value)
        }
    }

    fun appendDigit(digit: Char) {
        provideFeedback(digit)
        val newNum = _enteredNumber.value + digit
        _enteredNumber.value = newNum
        updateT9Matches(newNum)
    }

    fun deleteLastDigit() {
        if (_enteredNumber.value.isNotEmpty()) {
            provideHaptic()
            val newNum = _enteredNumber.value.dropLast(1)
            _enteredNumber.value = newNum
            updateT9Matches(newNum)
        }
    }

    fun clearNumber() {
        provideHaptic()
        _enteredNumber.value = ""
        _matchedContacts.value = emptyList()
    }

    fun setNumber(number: String) {
        _enteredNumber.value = number
        updateT9Matches(number)
    }

    private fun updateT9Matches(digits: String) {
        if (digits.isBlank()) {
            _matchedContacts.value = emptyList()
            return
        }
        if (settings.value.t9SearchEnabled) {
            _matchedContacts.value = T9SearchEngine.search(_allContacts.value, digits)
        }
    }

    fun makeCall(number: String = _enteredNumber.value): Boolean {
        if (number.isBlank()) return false
        provideHaptic()
        return telecomRepository.makeCall(number)
    }

    private fun provideFeedback(digit: Char) {
        provideHaptic()
        if (settings.value.dialpadTones) {
            val tone = when (digit) {
                '0' -> ToneGenerator.TONE_DTMF_0
                '1' -> ToneGenerator.TONE_DTMF_1
                '2' -> ToneGenerator.TONE_DTMF_2
                '3' -> ToneGenerator.TONE_DTMF_3
                '4' -> ToneGenerator.TONE_DTMF_4
                '5' -> ToneGenerator.TONE_DTMF_5
                '6' -> ToneGenerator.TONE_DTMF_6
                '7' -> ToneGenerator.TONE_DTMF_7
                '8' -> ToneGenerator.TONE_DTMF_8
                '9' -> ToneGenerator.TONE_DTMF_9
                '*' -> ToneGenerator.TONE_DTMF_S
                '#' -> ToneGenerator.TONE_DTMF_P
                else -> -1
            }
            if (tone != -1) {
                try {
                    toneGenerator?.startTone(tone, 120)
                } catch (_: Exception) {}
            }
        }
    }

    private fun provideHaptic() {
        if (settings.value.hapticFeedback && vibrator?.hasVibrator() == true) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(18)
                }
            } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
