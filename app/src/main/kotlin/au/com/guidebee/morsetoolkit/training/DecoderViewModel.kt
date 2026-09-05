package au.com.guidebee.morsetoolkit.training

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import au.com.guidebee.morsetoolkit.ConfigInfo
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder
import au.com.guidebee.morsetoolkit.decoder.MorseCodePatternMatch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val WAVEFORM_WIDTH = 800

/** Confirmed by device testing: at full volume the speaker clips the mic at close range. */
private const val SAMPLE_PLAYBACK_VOLUME = 0.3f

/**
 * Owns everything the Decoder screen needs to survive rotation/navigation
 * instead of living in Compose `remember{}` (which tears down and rebuilds
 * the mic pipeline, and loses decoded output, on every configuration
 * change): the [AudioDecoderController], the sample [MediaPlayer], and all
 * UI state, exposed as a single [StateFlow]. `onCleared()` - which only
 * fires when the screen is actually popped, not on rotation - is what
 * finally tears the recorder down.
 */
class DecoderViewModel(application: Application) : AndroidViewModel(application) {

    data class UiState(
        val hasPermission: Boolean = false,
        val isRecording: Boolean = false,
        val showAnalog: Boolean = false,
        val xStep: Float = 2f,
        val yScale: Float = 1f,
        val outputText: String = "",
        val savedData: List<Int> = emptyList(),
        val savedRawData: List<Int> = emptyList(),
        val maxMagnitude: Double = 1.0,
        val currentWpm: Float = 0f,
        val isPlayingSample: Boolean = false,
        val detectionMode: AudioMorseCodeDecoder.DetectionMode = AudioMorseCodeDecoder.DetectionMode.BROADBAND
    )

    private val _uiState = MutableStateFlow(UiState(detectionMode = DetectionModePreference.get(application)))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var lastChar = ' '
    private var mediaPlayer: MediaPlayer? = null
    private var controller = newController(_uiState.value.detectionMode)
    private var captureJob: Job? = null

    private fun newController(mode: AudioMorseCodeDecoder.DetectionMode): AudioDecoderController {
        val c = AudioDecoderController(ConfigInfo.sampleRate, WAVEFORM_WIDTH, mode = mode)
        c.decoder.addListener(object : MorseCodePatternMatch.MorseCodeListener {
            override fun onEmit(character: Char) {
                if (!(character == ' ' && lastChar == ' ') && character != '^') {
                    _uiState.update { it.copy(outputText = (it.outputText + character).takeLast(6000)) }
                }
                lastChar = character
            }

            override fun onCharStart() {}

            override fun onCharEnd(dotOrDash: String, length: Int) {
                if (dotOrDash == ".") {
                    val wpm = (((ConfigInfo.sampleRate / length.toFloat()) + 0.5f).toInt()) / 100f
                    _uiState.update { it.copy(currentWpm = wpm) }
                }
            }
        })
        c.decoder.addDataBufferListener { rawData, transformedData ->
            _uiState.update {
                it.copy(
                    savedData = transformedData.toList(),
                    savedRawData = rawData.toList(),
                    maxMagnitude = c.decoder.maxMagnitude
                )
            }
        }
        return c
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
    }

    fun setRecording(on: Boolean) {
        if (on && _uiState.value.hasPermission) {
            val started = controller.prepare()
            if (started) {
                captureJob = viewModelScope.launch { controller.capture() }
            }
            _uiState.update { it.copy(isRecording = started) }
        } else {
            captureJob?.cancel()
            captureJob = null
            controller.release()
            _uiState.update { it.copy(isRecording = false, savedData = emptyList(), savedRawData = emptyList()) }
        }
    }

    fun switchDetectionMode(newMode: AudioMorseCodeDecoder.DetectionMode) {
        if (newMode == _uiState.value.detectionMode) return
        stopSample()
        captureJob?.cancel()
        captureJob = null
        controller.release()
        controller = newController(newMode)
        DetectionModePreference.set(getApplication(), newMode)
        _uiState.update {
            it.copy(
                detectionMode = newMode,
                isRecording = false,
                savedData = emptyList(),
                savedRawData = emptyList(),
                outputText = ""
            )
        }
    }

    fun playSample() {
        if (mediaPlayer != null) return
        controller.decoder.resetMaxMagnitude()
        controller.decoder.changeDotLimit(23f)
        if (!_uiState.value.isRecording) setRecording(true)
        val mp = MediaPlayer.create(getApplication(), R.raw.morse)
        mp.setVolume(SAMPLE_PLAYBACK_VOLUME, SAMPLE_PLAYBACK_VOLUME)
        mp.setOnCompletionListener {
            it.stop()
            it.release()
            mediaPlayer = null
            _uiState.update { state -> state.copy(isPlayingSample = false) }
        }
        mediaPlayer = mp
        _uiState.update { it.copy(isPlayingSample = true) }
        mp.start()
    }

    fun stopSample() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
        _uiState.update { it.copy(isPlayingSample = false) }
    }

    fun resetDecoder() {
        controller.decoder.resetMaxMagnitude()
        controller.decoder.changeDotLimit(23f)
    }

    fun clearOutput() {
        _uiState.update { it.copy(outputText = "") }
    }

    fun setShowAnalog(show: Boolean) {
        _uiState.update { it.copy(showAnalog = show) }
    }

    fun setXStep(step: Float) {
        _uiState.update { it.copy(xStep = step) }
    }

    fun setYScale(scale: Float) {
        _uiState.update { it.copy(yScale = scale) }
    }

    override fun onCleared() {
        captureJob?.cancel()
        controller.release()
        stopSample()
    }
}
