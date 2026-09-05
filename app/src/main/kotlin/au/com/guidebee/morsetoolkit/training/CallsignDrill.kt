package au.com.guidebee.morsetoolkit.training

import kotlin.random.Random

/**
 * Generates plausible-format practice callsigns (valid prefix + digit +
 * suffix shapes) rather than reusing any real station's assigned callsign.
 */
object CallsignDrill {
    private val prefixes = listOf(
        "W", "K", "N", "VK", "VE", "G", "M", "JA", "DL", "F", "EA", "PY", "ZL", "ZS"
    )
    private val suffixLetters = ('A'..'Z').toList()

    fun generate(random: Random = Random.Default): String {
        val prefix = prefixes.random(random)
        val digit = random.nextInt(0, 10)
        val suffixLength = random.nextInt(2, 4)
        val suffix = (1..suffixLength).joinToString("") { suffixLetters.random(random).toString() }
        return "$prefix$digit$suffix"
    }

    fun generateSet(count: Int, random: Random = Random.Default): List<String> =
        (1..count).map { generate(random) }
}
