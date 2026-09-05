package au.com.guidebee.morsetoolkit.training

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Mic capture + FFT decode, unchanged from ListenActivity — just lifted out
 * of the Activity so a Compose screen can own it. Caller must already hold
 * RECORD_AUDIO before calling [start].
 */
class AudioDecoderController(
    private val sampleRate: Int,
    waveformWidth: Int,
    frameSpeed: Int = 50
) {

    val decoder = AudioMorseCodeDecoder(23, waveformWidth, frameSpeed, 1)

    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )
    private var audioRecord: AudioRecord? = null
    private val recording = AtomicBoolean(false)
    private var recordThread: Thread? = null

    /** Returns false if the recorder failed to initialize (caller should surface an error). */
    fun start(): Boolean {
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
            record.startRecording()
            recording.set(true)
            recordThread = thread(name = "AudioDecoderController") {
                val buffer = ShortArray(minBufferSize)
                while (recording.get()) {
                    val n = record.read(buffer, 0, minBufferSize)
                    if (n > 0) decoder.processAudioBuffer(buffer, n)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun stop() {
        recording.set(false)
        try {
            recordThread?.join(200)
        } catch (_: InterruptedException) {
            // best-effort join, fall through to release
        }
        recordThread = null
        audioRecord?.let {
            try {
                it.stop()
            } catch (_: IllegalStateException) {
                // wasn't recording
            }
            it.release()
        }
        audioRecord = null
    }
}
