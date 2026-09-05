package au.com.guidebee.morsetoolkit.training

data class QsoLine(val label: String, val morseText: String)

data class QsoScript(val title: String, val lines: List<QsoLine>)

/**
 * Canned practice QSO exchanges built from generated (non-real) callsigns —
 * the same shape as a real contact: a CQ call, a reply, a signal report,
 * then a sign-off.
 */
object QsoScripts {
    fun sample(): List<QsoScript> {
        val a = CallsignDrill.generate()
        val b = CallsignDrill.generate()
        return listOf(
            QsoScript(
                title = "First contact",
                lines = listOf(
                    QsoLine("$a calls CQ", "CQ CQ CQ DE $a $a K"),
                    QsoLine("$b replies", "$a DE $b K"),
                    QsoLine(
                        "$a sends a report",
                        "$b DE $a UR RST 599 599 NAME OM OM BT QTH IS SYDNEY SYDNEY BT HW? $b DE $a K"
                    ),
                    QsoLine("$b signs off", "$a DE $b R FB TNX QSO 73 73 SK")
                )
            ),
            QsoScript(
                title = "Signal report exchange",
                lines = listOf(
                    QsoLine("Report", "UR RST IS 579 579 BT"),
                    QsoLine("Sign-off", "TNX FER QSO GL 73 SK")
                )
            )
        )
    }
}
