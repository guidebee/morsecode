package au.com.guidebee.morsetoolkit.training

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import au.com.guidebee.morsetoolkit.ConfigInfo
import au.com.guidebee.morsetoolkit.activity.R

/**
 * Tap-feedback sounds for the on-screen key: the same res/raw/dit.wav and
 * dah.wav clips MorseActivity's physical key already plays, gated by the
 * same [ConfigInfo.playAudio] / [ConfigInfo.audioVolume] settings from the
 * classic Options screen, so the new and legacy UIs share one audio
 * preference instead of the new screens silently ignoring it.
 */
class KeyClickSounds(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ditId = soundPool.load(context, R.raw.dit, 1)
    private val dahId = soundPool.load(context, R.raw.dah, 1)

    fun playDit() = play(ditId)
    fun playDah() = play(dahId)

    private fun play(soundId: Int) {
        if (!ConfigInfo.playAudio) return
        val volume = (ConfigInfo.audioVolume + 1) / 100f
        soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
