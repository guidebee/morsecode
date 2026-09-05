package au.com.guidebee.morsetoolkit.training

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Mic capture + decode, unchanged from ListenActivity in substance - just
 * lifted out of the Activity so a Compose screen can own it, and using a
 * suspend function instead of a raw Thread for the read loop so its
 * lifetime is tied to a coroutine (cancelling that coroutine is how you
 * stop capture) instead of manual Thread/AtomicBoolean bookkeeping. Caller
 * must already hold RECORD_AUDIO before calling [prepare].
 */
class AudioDecoderController(
    private val sampleRate: Int,
    waveformWidth: Int,
    frameSpeed: Int = 50,
    mode: AudioMorseCodeDecoder.DetectionMode = AudioMorseCodeDecoder.DetectionMode.BROADBAND
) {

    val decoder = AudioMorseCodeDecoder(23, waveformWidth, frameSpeed, sampleRate, mode)

    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )
    private var audioRecord: AudioRecord? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var automaticGainControl: AutomaticGainControl? = null

    /**
     * The Android HAL can auto-attach acoustic echo cancellation, noise
     * suppression and AGC to a MIC session. AEC in particular is built to
     * cancel out sound the device just played through its own speaker -
     * exactly what "Play Sample" needs the mic to *hear*, since it plays a
     * tone through the speaker and listens for that same tone. Left enabled,
     * it actively fights the signal this decoder depends on; a source
     * external to the device (a PC speaker, a real transceiver) has no such
     * "echo" to cancel, which is why capture from those is clean while
     * on-device self-playback isn't.
     */
    private fun disableInterferingAudioEffects(sessionId: Int) {
        if (AcousticEchoCanceler.isAvailable()) {
            echoCanceler = AcousticEchoCanceler.create(sessionId)?.apply { enabled = false }
        }
        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(sessionId)?.apply { enabled = false }
        }
        if (AutomaticGainControl.isAvailable()) {
            automaticGainControl = AutomaticGainControl.create(sessionId)?.apply { enabled = false }
        }
    }

    /**
     * Initializes and starts the recorder, but doesn't run the read loop
     * itself - launch [capture] in a coroutine after this returns true, and
     * cancel that coroutine (then call [release]) to stop.
     */
    fun prepare(): Boolean {
        return try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )
            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                return false
            }
            audioRecord = record
            disableInterferingAudioEffects(record.audioSessionId)
            record.startRecording()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reads from the mic and feeds [decoder] until the calling coroutine is
     * cancelled. Runs on [Dispatchers.IO]; [prepare] must have already
     * returned true.
     */
    suspend fun capture() {
        val record = audioRecord ?: return
        withContext(Dispatchers.IO) {
            val buffer = ShortArray(minBufferSize)
            while (isActive) {
                val n = record.read(buffer, 0, minBufferSize)
                if (n > 0) decoder.processAudioBuffer(buffer, n)
            }
        }
    }

    /** Stops and releases the recorder and its audio effects. Cancel the [capture] coroutine first. */
    fun release() {
        audioRecord?.let {
            try {
                it.stop()
            } catch (_: IllegalStateException) {
                // wasn't recording
            }
            it.release()
        }
        audioRecord = null
        echoCanceler?.release()
        echoCanceler = null
        noiseSuppressor?.release()
        noiseSuppressor = null
        automaticGainControl?.release()
        automaticGainControl = null
    }
}
