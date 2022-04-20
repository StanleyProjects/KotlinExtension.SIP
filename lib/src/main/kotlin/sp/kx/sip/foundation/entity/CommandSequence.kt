package sp.kx.sip.foundation.entity

/**
 * https://datatracker.ietf.org/doc/html/rfc3261#section-8.1.1.5
 */
data class CommandSequence(
    val number: Int,
    val method: String
)
