package au.com.guidebee.morsetoolkit.training

data class ProcedureEntry(val code: String, val meaning: String)

/** Q-codes and prosigns used in real CW operating — standard ham-radio reference material, not invented for this app. */
object ContentPacks {
    val qCodes: List<ProcedureEntry> = listOf(
        ProcedureEntry("QRZ", "Who is calling me?"),
        ProcedureEntry("QTH", "My location is…"),
        ProcedureEntry("QSL", "I confirm receipt"),
        ProcedureEntry("QRM", "You are being interfered with by other stations"),
        ProcedureEntry("QRN", "You are troubled by atmospheric noise"),
        ProcedureEntry("QSY", "Change to another frequency"),
        ProcedureEntry("QRV", "I am ready"),
        ProcedureEntry("QRX", "Please wait, stand by"),
        ProcedureEntry("QRT", "Stop sending"),
        ProcedureEntry("QSB", "Your signal is fading"),
        ProcedureEntry("QRP", "Reduce power"),
        ProcedureEntry("QRO", "Increase power"),
        ProcedureEntry("QRQ", "Send faster"),
        ProcedureEntry("QRS", "Send more slowly"),
        ProcedureEntry("QSO", "A conversation, a contact")
    )

    val prosigns: List<ProcedureEntry> = listOf(
        ProcedureEntry("AR", "End of message"),
        ProcedureEntry("SK", "End of contact (silent key)"),
        ProcedureEntry("BT", "Break, new section"),
        ProcedureEntry("KN", "Over, to a named station only"),
        ProcedureEntry("AS", "Please wait"),
        ProcedureEntry("CQ", "Calling any station")
    )
}
